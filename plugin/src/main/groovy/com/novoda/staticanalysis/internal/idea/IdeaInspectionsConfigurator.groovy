package com.novoda.staticanalysis.internal.idea


import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask

class IdeaInspectionsConfigurator extends CodeQualityConfigurator<SourceTask, CodeQualityExtension> {

    private static final String IDEA_PLUGIN = 'org.jetbrains.intellij.inspections'

    static IdeaInspectionsConfigurator create(Project project,
                                              NamedDomainObjectContainer<Violations> violationsContainer,
                                              Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('idea-inspections')
        return new IdeaInspectionsConfigurator(project, violations, evaluateViolations)
    }

    IdeaInspectionsConfigurator(Project project, Violations violations, Task evaluateViolations) {
        super(project, violations, evaluateViolations)
    }

    @Override
    protected String getToolName() {
        'inspections'
    }

    @Override
    protected Object getToolPlugin() {
        return IDEA_PLUGIN
    }

    @Override
    protected Class<CodeQualityExtension> getExtensionClass() {
        Class.forName('org.jetbrains.intellij.extensions.InspectionPluginExtension')
    }

    @Override
    protected Action<CodeQualityExtension> getDefaultConfiguration() {
        return { extension ->
            extension.ignoreFailures = true
        }
    }

    @Override
    protected Class<SourceTask> getTaskClass() {
        Class.forName('org.jetbrains.intellij.tasks.InspectionsTask')
    }

    @Override
    protected void configureAndroidVariant(variant) {
        project.with {
            variant.sourceSets.each { sourceSet ->
                def task = tasks.maybeCreate("inspections${sourceSet.name.capitalize()}", taskClass)
                task.description = "Run Idea Inspections analysis for ${sourceSet.name} classes"
                task.source = sourceSet.java.srcDirs
                task.classpath = files("$buildDir/intermediates/classes/")
                task.mustRunAfter variant.javaCompile
            }
        }
    }

    @Override
    protected void configureReportEvaluation(SourceTask inspectionsTask, Violations violations) {
        def collectViolations = createCollectViolationsTask(inspectionsTask, violations)
        collectViolations.dependsOn inspectionsTask
        evaluateViolations.dependsOn collectViolations
    }

    private def createCollectViolationsTask(inspectionsTask, Violations violations) {
        def task = project.tasks.maybeCreate("collect${inspectionsTask.name.capitalize()}Violations", CollectIdeaInspectionsViolationsTask)
        task.xmlReportFile = inspectionsTask.reports.xml.destination
        task.htmlReportFile = inspectionsTask.reports.html.destination
        task.violations = violations
        task
    }
}
