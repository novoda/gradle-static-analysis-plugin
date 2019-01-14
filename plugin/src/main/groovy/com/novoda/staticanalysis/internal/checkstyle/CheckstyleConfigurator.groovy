package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

import static com.novoda.staticanalysis.internal.TasksCompat.maybeCreateTask

class CheckstyleConfigurator extends CodeQualityConfigurator<Checkstyle, CheckstyleExtension> {

    static CheckstyleConfigurator create(Project project, NamedDomainObjectContainer<Violations> violationsContainer) {
        Violations violations = violationsContainer.maybeCreate('Checkstyle')
        return new CheckstyleConfigurator(project, violations)
    }

    private CheckstyleConfigurator(Project project, Violations violations) {
        super(project, violations)
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
        return { extension ->
            extension.toolVersion = '7.1.2'
        }
    }

    @Override
    protected Class<Checkstyle> getTaskClass() {
        Checkstyle
    }

    @Override
    protected void configureAndroidVariant(variant) {
        variant.sourceSets.each { sourceSet ->
            def taskName = "checkstyle${sourceSet.name.capitalize()}"

            maybeCreateTask(project, taskName, Checkstyle) { Checkstyle task ->
                task.description = "Run Checkstyle analysis for ${sourceSet.name} classes"
                task.source = sourceSet.java.srcDirs
                task.classpath = project.files("$project.buildDir/intermediates/classes/")
                task.exclude '**/*.kt'
                sourceFilter.applyTo(task)
                task.mustRunAfter variant.javaCompile
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Checkstyle checkstyle, Violations violations) {
        checkstyle.showViolations = false
        checkstyle.ignoreFailures = true
        checkstyle.metaClass.getLogger = { QuietLogger.INSTANCE }

        createCollectViolationsTask(checkstyle, violations)
    }

    private def createCollectViolationsTask(Checkstyle checkstyle, Violations violations) {
        maybeCreateTask(project, "collect${checkstyle.name.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = checkstyle.reports.xml.destination
            task.violations = violations
            task.dependsOn(checkstyle)
        }
    }
}
