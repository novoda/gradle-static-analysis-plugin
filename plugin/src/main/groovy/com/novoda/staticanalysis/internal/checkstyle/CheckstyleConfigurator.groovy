package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

class CheckstyleConfigurator extends CodeQualityConfigurator<Checkstyle, CheckstyleExtension> {

    static CheckstyleConfigurator create(Project project,
                                         NamedDomainObjectContainer<Violations> violationsContainer,
                                         EvaluateViolationsTask evaluateViolationsTask) {
        Violations violations = violationsContainer.maybeCreate('Checkstyle')
        return new CheckstyleConfigurator(project, violations, evaluateViolationsTask)
    }

    private CheckstyleConfigurator(Project project, Violations violations, EvaluateViolationsTask evaluateViolationsTask) {
        super(project, violations, evaluateViolationsTask)
    }

    @Override
    protected String getToolName() {
        'checkstyle'
    }

    @Override
    protected Class<CheckstyleExtension> getExtensionClass() {
        CheckstyleExtension
    }

    @Override
    protected Action<CheckstyleExtension> getDefaultConfiguration() {
        new Action<CheckstyleExtension>() {
            @Override
            void execute(CheckstyleExtension checkstyleExtension) {
                checkstyleExtension.toolVersion = '7.1.2'
            }
        }
    }

    @Override
    protected Class<Checkstyle> getTaskClass() {
        Checkstyle
    }

    @Override
    protected void configureAndroidProject(NamedDomainObjectSet variants) {
        project.with {
            variants.all { variant ->
                variant.sourceSets.each { sourceSet ->
                    def taskName = "checkstyle${sourceSet.name.capitalize()}"
                    Checkstyle task = tasks.findByName(taskName)
                    if (task == null) {
                        task = tasks.create(taskName, Checkstyle)
                        task.with {
                            description = "Run Checkstyle analysis for ${sourceSet.name} classes"
                            source = sourceSet.java.srcDirs
                            classpath = files("$buildDir/intermediates/classes/")
                        }
                    }
                    sourceFilter.applyTo(task)
                    task.mustRunAfter variant.javaCompile
                }
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Checkstyle checkstyle, Violations violations) {
        checkstyle.showViolations = false
        checkstyle.ignoreFailures = true
        checkstyle.metaClass.getLogger = { QuietLogger.INSTANCE }

        def collectViolations = createCollectViolationsTask(checkstyle, violations)

        evaluateViolations.dependsOn collectViolations
        collectViolations.dependsOn checkstyle
    }

    private CollectCheckstyleViolationsTask createCollectViolationsTask(Checkstyle checkstyle, Violations violations) {
        project.tasks.create("collect${checkstyle.name.capitalize()}Violations", CollectCheckstyleViolationsTask) { collectViolations ->
            collectViolations.xmlReportFile = checkstyle.reports.xml.destination
            collectViolations.violations = violations
        }
    }
}
