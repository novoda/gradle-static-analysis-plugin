package com.novoda.staticanalysis.internal.detekt

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.checkstyle.CollectCheckstyleViolationsTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import static com.novoda.staticanalysis.internal.TasksCompat.maybeCreateTask

class DetektConfigurator implements Configurator {

    private static final String DETEKT_PLUGIN = 'io.gitlab.arturbosch.detekt'
    private static final String LAST_COMPATIBLE_DETEKT_VERSION = '1.0.0-RC12'
    private static final String DETEKT_NOT_APPLIED = 'The Detekt plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/arturbosch/detekt.'
    private static final String OUTPUT_NOT_DEFINED = 'Output not defined! To analyze the results, `output` needs to be defined in Detekt profile.'
    private static final String DETEKT_CONFIGURATION_ERROR = "A problem occurred while configuring Detekt. Please make sure to use a compatible version (All versions up to $LAST_COMPATIBLE_DETEKT_VERSION)"
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to enable "reports.xml" in your Detekt configuration'

    private final Project project
    private final Violations violations

    static DetektConfigurator create(Project project, NamedDomainObjectContainer<Violations> violationsContainer) {
        Violations violations = violationsContainer.maybeCreate('Detekt')
        return new DetektConfigurator(project, violations)
    }

    private DetektConfigurator(Project project, Violations violations) {
        this.project = project
        this.violations = violations
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.detekt = { Closure config ->
            if (!isKotlinProject(project)) {
                return
            }

            if (!project.plugins.hasPlugin(DETEKT_PLUGIN)) {
                throw new GradleException(DETEKT_NOT_APPLIED)
            }

            def detekt = project.extensions['detekt']
            setDefaultXmlReport(detekt)
            config.delegate = detekt
            config()

            configureToolTask(detekt)
        }
    }

    private void setDefaultXmlReport(detekt) {
        if (detekt.hasProperty('reports')) {
            detekt.reports {
                xml.enabled = true
                xml.destination = new File(project.buildDir, 'reports/detekt/detekt.xml')
            }
        }
    }

    private def configureToolTask(detekt) {
        def detektTask = project.tasks.findByName('detekt')
        if (detektTask?.hasProperty('reports')) {
            def reports = detektTask.reports
            if (!reports.xml.enabled) {
                throw new IllegalStateException(XML_REPORT_NOT_ENABLED)
            }
            return createCollectViolationsTask(
                    violations,
                    detektTask,
                    reports.xml.destination,
                    reports.html.destination
            )
        }

        // Fallback to old Detekt versions
        def output = resolveOutput(detekt)
        if (!output) {
            throw new IllegalArgumentException(OUTPUT_NOT_DEFINED)
        }
        def outputFolder = project.file(output)
        return createCollectViolationsTask(
                violations,
                project.tasks['detektCheck'],
                new File(outputFolder, 'detekt-checkstyle.xml'),
                new File(outputFolder, 'detekt-report.html')
        )
    }

    private static resolveOutput(detekt) {
        if (detekt.hasProperty('profileStorage')) {
            detekt.profileStorage.systemOrDefault.output
        } else if (detekt.respondsTo('systemOrDefaultProfile')) {
            detekt.systemOrDefaultProfile().output
        } else {
            throw new IllegalStateException(DETEKT_CONFIGURATION_ERROR)
        }
    }

    private def createCollectViolationsTask(Violations violations, detektTask, File xmlReportFile, File htmlReportFile) {
        maybeCreateTask(project, 'collectDetektViolations', CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = xmlReportFile
            task.htmlReportFile = htmlReportFile
            task.violations = violations
            task.dependsOn(detektTask)
        }
    }

    private static boolean isKotlinProject(final Project project) {
        final boolean isKotlin = project.plugins.hasPlugin('kotlin')
        final boolean isKotlinAndroid = project.plugins.hasPlugin('kotlin-android')
        final boolean isKotlinPlatformCommon = project.plugins.hasPlugin('kotlin-platform-common')
        final boolean isKotlinMultiplatform = project.plugins.hasPlugin('org.jetbrains.kotlin.multiplatform')
        final boolean isKotlinPlatformJvm = project.plugins.hasPlugin('kotlin-platform-jvm')
        final boolean isKotlinPlatformJs = project.plugins.hasPlugin('kotlin-platform-js')
        return isKotlin || isKotlinAndroid || isKotlinPlatformCommon || isKotlinMultiplatform || isKotlinPlatformJvm || isKotlinPlatformJs
    }
}
