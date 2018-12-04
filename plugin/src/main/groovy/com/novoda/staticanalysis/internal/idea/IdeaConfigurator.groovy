package com.novoda.staticanalysis.internal.idea

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.checkstyle.CollectCheckstyleViolationsTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

class IdeaConfigurator implements Configurator {

    private static final String IDEA_PLUGIN = 'org.jetbrains.intellij.inspections'
    private static final String IDEA_NOT_APPLIED = 'The Idea Inspections plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JetBrains/inspection-plugin'

    private final Project project
    private final Violations violations
    private final Task evaluateViolations

    static IdeaConfigurator create(Project project,
                                   NamedDomainObjectContainer<Violations> violationsContainer,
                                   Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('inspections')
        return new IdeaConfigurator(project, violations, evaluateViolations)
    }

    IdeaConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.inspections = { Closure config ->
            if (!project.plugins.hasPlugin(IDEA_PLUGIN)) {
                throw new GradleException(IDEA_NOT_APPLIED)
            }

            configureExtension(config)

            project.afterEvaluate {
                project.plugins.withId("kotlin") {
                    configureKotlinProject()
                }
                project.plugins.withId("kotlin2js") {
                    configureKotlinProject()
                }
                project.plugins.withId("kotlin-platform-common") {
                    configureKotlinProject()
                }
                project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
                    configureKotlinProject()
                }
            }
        }
    }

    private void configureExtension(Closure config) {
        def inspections = project.inspections
        inspections.ignoreFailures = true

        config.delegate = inspections
        config()
    }

    private void configureKotlinProject() {
        project.sourceSets.each { configureInspections(it.name) }
    }

    private void configureInspections(def sourceSetName) {
        project.tasks.matching {
            it.name == "inspections${sourceSetName.capitalize()}"
        }.all { Task inspectionsTask ->
            def collectViolations = createCollectViolationsTask(
                    violations,
                    sourceSetName,
                    inspectionsTask.reports.xml.destination,
                    inspectionsTask.reports.html.destination
            )
            collectViolations.dependsOn inspectionsTask
            evaluateViolations.dependsOn collectViolations
        }
    }

    private def createCollectViolationsTask(Violations violations, def sourceSetName, File xmlReportFile, File txtReportFile) {
        project.tasks.create("collectInspections${sourceSetName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = xmlReportFile
            task.htmlReportFile = txtReportFile
            task.violations = violations
        }
    }
}
