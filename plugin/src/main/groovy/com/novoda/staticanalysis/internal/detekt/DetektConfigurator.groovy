package com.novoda.staticanalysis.internal.detekt

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

class DetektConfigurator implements Configurator {

    private static final String DETEKT_PLUGIN = 'io.gitlab.arturbosch.detekt'
    private static final String DETEKT_NOT_APPLIED = 'The detekt plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/arturbosch/detekt.'

    private final Project project
    private final Violations violations
    private final Task evaluateViolations

    static DetektConfigurator create(Project project,
                                     NamedDomainObjectContainer<Violations> violationsContainer,
                                     Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('Detekt')
        return new DetektConfigurator(project, violations, evaluateViolations)
    }

    private DetektConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."detekt" = { Closure config ->

            if (!isKotlinProject(project)) {
                return
            }

            if (!project.plugins.hasPlugin(DETEKT_PLUGIN)) {
                throw new GradleException(DETEKT_NOT_APPLIED)
            }

            project.extensions.findByName('detekt').with {
                // apply configuration closure to detekt extension
                config.delegate = it
                config()
            }

            configureToolTask()
        }
    }

    private void configureToolTask() {
        def detektTask = project.tasks['detektCheck']
        // run detekt as part of check
        project.tasks['check'].dependsOn(detektTask)

        // evaluate violations after detekt
        detektTask.group = 'verification'
        def collectViolations = createCollectViolationsTask(violations)
        evaluateViolations.dependsOn collectViolations
        collectViolations.dependsOn detektTask
    }

    private CollectDetektViolationsTask createCollectViolationsTask(Violations violations) {
        def outputFolder = project.file(project.extensions.findByName('detekt').systemOrDefaultProfile().output)
        project.tasks.create("collectDetektViolations", CollectDetektViolationsTask) { collectViolations ->
            collectViolations.xmlReportFile = new File(outputFolder, 'detekt-checkstyle.xml')
            collectViolations.htmlReportFile = new File(outputFolder, 'detekt-report.html')
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
