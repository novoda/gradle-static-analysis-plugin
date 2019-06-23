package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.EvaluateToolViolationsTask
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class CheckstyleConfigurator extends CodeQualityConfigurator<Checkstyle, CheckstyleExtension> {

    static CheckstyleConfigurator create(Project project,
                                         EvaluateToolViolationsTask evaluateViolations) {
        return new CheckstyleConfigurator(project, evaluateViolations.toolViolations, evaluateViolations)
    }

    private CheckstyleConfigurator(Project project,
                                   Violations violations,
                                   Task evaluateViolations) {
        super(project, violations, evaluateViolations)
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
    protected void createToolTaskForAndroid(sourceSet) {
        createTask(project, getToolTaskNameFor(sourceSet), Checkstyle) { task ->
            task.description = "Run Checkstyle analysis for ${sourceSet.name} classes"
            task.source = sourceSet.java.srcDirs
            task.classpath = project.files("${project.buildDir}/intermediates/classes/")
        }
    }

    @Override
    protected void configureToolTask(Checkstyle task) {
        super.configureToolTask(task)
        task.showViolations = false
    }

    @Override
    protected def createCollectViolations(String taskName, Violations violations) {
        createTask(project, "collect${taskName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            def checkstyle = project.tasks[taskName] as Checkstyle
            task.xmlReportFile = checkstyle.reports.xml.destination
            task.violations = violations
            task.dependsOn(checkstyle)
        }
    }
}
