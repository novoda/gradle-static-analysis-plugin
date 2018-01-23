package com.novoda.staticanalysis.internal.detekt

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Project
import org.gradle.api.Task

public class DetektConfigurator {

    private final Project project
    private final Violations violations
    private final Task evaluateViolations

    public DetektConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."detekt" = { Closure config ->

            if (!isKotlinProject(project)) {
                return
            }

            project.apply plugin: 'io.gitlab.arturbosch.detekt'

            project.extensions.findByName('detekt').with {
                // apply configuration closure to detekt extension
                config.delegate = it
                config()
            }

            if (project.tasks.findByName('detektCheck')) {
                configureToolTask()
            }
        }
    }

    private void configureToolTask() {
        def detektTask = project.tasks.findByName('detektCheck')
        // run detekt as part of check
        project.tasks.findByName('check').dependsOn(detektTask)

        // evaluate violations after detekt
        detektTask.group = 'verification'
        def collectViolations = createCollectViolationsTask(violations)
        evaluateViolations.dependsOn collectViolations
        collectViolations.dependsOn detektTask
    }

    private CollectDetektViolationsTask createCollectViolationsTask(Violations violations) {
        def reportFilePath = "${project.extensions.findByName('detekt').systemOrDefaultProfile().output}/detekt-checkstyle.xml"

        project.tasks.create("collectDetektViolations", CollectDetektViolationsTask) { collectViolations ->
            collectViolations.xmlReportFile = new File(reportFilePath)
            collectViolations.violations = violations
        }
    }

    private static boolean isKotlinProject(final Project project) {
        final boolean isKotlin = project.plugins.hasPlugin('kotlin')
        final boolean isKotlinAndroid = project.plugins.hasPlugin('kotlin-android')
        final boolean isKotlinPlatformCommon = project.plugins.hasPlugin('kotlin-platform-common')
        final boolean isKotlinPlatformJvm = project.plugins.hasPlugin('kotlin-platform-jvm')
        final boolean isKotlinPlatformJs = project.plugins.hasPlugin('kotlin-platform-js')
        return isKotlin || isKotlinAndroid || isKotlinPlatformCommon || isKotlinPlatformJvm || isKotlinPlatformJs
    }
}
