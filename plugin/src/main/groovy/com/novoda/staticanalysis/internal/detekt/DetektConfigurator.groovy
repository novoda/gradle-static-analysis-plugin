package com.novoda.staticanalysis.internal.detekt

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.checkstyle.CollectCheckstyleViolationsTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class DetektConfigurator implements Configurator {

    private static final String DETEKT_PLUGIN = 'io.gitlab.arturbosch.detekt'
    private static final String LAST_COMPATIBLE_DETEKT_VERSION = '1.0.0-RC14'
    private static final String DETEKT_NOT_APPLIED = 'The Detekt plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/arturbosch/detekt.'
    private static final String DETEKT_CONFIGURATION_ERROR = "A problem occurred while configuring Detekt. Please make sure to use a compatible version (All versions up to $LAST_COMPATIBLE_DETEKT_VERSION)"
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to enable "reports.xml" in your Detekt configuration'

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
        project.extensions.findByType(StaticAnalysisExtension).ext.detekt = { Closure config ->
            if (!isKotlinProject(project)) {
                return
            }

            if (!project.plugins.hasPlugin(DETEKT_PLUGIN)) {
                throw new GradleException(DETEKT_NOT_APPLIED)
            }

            def detekt = project.extensions.findByName('detekt')
            setDefaultXmlReport(detekt)
            config.delegate = detekt
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            disableFailFast(detekt)

            def collectViolations = createCollectViolationsTask(violations)
            evaluateViolations.dependsOn collectViolations
        }
    }

    private void setDefaultXmlReport(detekt) {
        try {
            detekt.reports {
                xml.enabled = true
                xml.destination = new File(project.buildDir, 'reports/detekt/detekt.xml')
            }
        } catch (Exception exception) {
            throw new GradleException(DETEKT_CONFIGURATION_ERROR, exception)
        }
    }

    private static void disableFailFast(detekt) {
        if (detekt.hasProperty('failFast')) {
            detekt.failFast = false
        }
    }

    private def createCollectViolationsTask(Violations violations) {
        createTask(project, 'collectDetektViolations', CollectCheckstyleViolationsTask) { task ->
            def detektTask = project.tasks.findByName('detekt')
            try {
                def reports = detektTask.reports
                checkXmlReportEnabled(reports)
                task.xmlReportFile = reports.xml.destination
                task.htmlReportFile = reports.html.destination
            } catch (Exception exception) {
                throw new GradleException(DETEKT_CONFIGURATION_ERROR, exception)
            }
            task.violations = violations
            task.dependsOn detektTask
        }
    }

    private void checkXmlReportEnabled(reports) {
        if (!reports.xml.enabled) {
            throw new IllegalStateException(XML_REPORT_NOT_ENABLED)
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
