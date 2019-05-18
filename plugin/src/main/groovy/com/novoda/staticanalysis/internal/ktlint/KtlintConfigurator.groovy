package com.novoda.staticanalysis.internal.ktlint

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantFilter
import com.novoda.staticanalysis.internal.checkstyle.CollectCheckstyleViolationsTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

import static com.novoda.staticanalysis.internal.Exceptions.handleException
import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class KtlintConfigurator implements Configurator {

    private static final String KTLINT_PLUGIN = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_NOT_APPLIED = 'The Ktlint plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JLLeitschuh/ktlint-gradle/#how-to-use'
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to add "CHECKSTYLE" into reports in your Ktlint configuration'

    private static final String LAST_COMPATIBLE_KTLINT_VERSION = '8.0.0'
    private static final String MIN_COMPATIBLE_KTLINT_VERSION = '6.2.1' // Do not forget to update ktlint.md
    private static final String KTLINT_CONFIGURATION_ERROR = """\
A problem occurred while configuring Ktlint. Please make sure to use a compatible version:
Minimum compatible Ktlint Plugin version: $MIN_COMPATIBLE_KTLINT_VERSION
Last tested compatible version: $LAST_COMPATIBLE_KTLINT_VERSION
"""

    private final Project project
    private final Violations violations
    private final Task evaluateViolations
    private final VariantFilter variantFilter
    protected boolean configured = false

    static KtlintConfigurator create(Project project,
                                     NamedDomainObjectContainer<Violations> violationsContainer,
                                     Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('ktlint')
        return new KtlintConfigurator(project, violations, evaluateViolations)
    }

    KtlintConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
        this.variantFilter = new VariantFilter(project)
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.ktlint = { Closure config ->
            if (!project.plugins.hasPlugin(KTLINT_PLUGIN)) {
                throw new GradleException(KTLINT_NOT_APPLIED)
            }

            configureKtlintExtension(config)

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
            project.plugins.withId('com.android.application') {
                configureAndroidWithVariants(variantFilter.filteredApplicationVariants)
            }
            project.plugins.withId('com.android.library') {
                configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
            }
        }
    }

    private void configureKtlintExtension(Closure config) {
        try {
            def ktlint = project.ktlint
            ktlint.ignoreFailures = true
            ktlint.ext.includeVariants = { Closure<Boolean> filter ->
                variantFilter.includeVariantsFilter = filter
            }
            config.delegate = ktlint
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        } catch (Exception exception) {
            handleException(KTLINT_CONFIGURATION_ERROR, exception)
        }
    }

    private void configureKotlinProject() {
        project.sourceSets.each {
            def collectViolations = createCollectViolationsTask(violations, it.name)
            evaluateViolations.dependsOn collectViolations
        }
    }

    private void configureAndroidWithVariants(def mainVariants) {
        if (configured) return

        project.android.sourceSets.all { sourceSet ->
            createCollectViolationsTask(violations, sourceSet.name)
        }
        mainVariants.all { configureAndroidVariant(it) }
        variantFilter.filteredTestVariants.all { configureAndroidVariant(it) }
        variantFilter.filteredUnitTestVariants.all { configureAndroidVariant(it) }
        configured = true
    }

    private void configureAndroidVariant(def variant) {
        def collectViolations = createVariantMetaTask(variant)
        evaluateViolations.dependsOn collectViolations
    }

    private def createVariantMetaTask(variant) {
        createTask(project, "collectKtlint${variant.name.capitalize()}VariantViolations", Task) { task ->
            variant.sourceSets.forEach { sourceSet ->
                task.dependsOn "collectKtlint${sourceSet.name.capitalize()}Violations"
            }
        }
    }

    private def createCollectViolationsTask(Violations violations, def sourceSetName) {
        createTask(project, "collectKtlint${sourceSetName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            Task ktlintTask = project.tasks.findByName("ktlint${sourceSetName.capitalize()}SourceSetCheck")
            if (ktlintTask == null) {
                ktlintTask = project.tasks.findByName("ktlint${sourceSetName.capitalize()}Check")
            }

            File xmlReportFile = null
            File txtReportFile = null
            try {
                ktlintTask.reportOutputFiles.each { key, fileProp ->
                    def file = fileProp.get().asFile
                    if (file.name.endsWith('.xml')) {
                        xmlReportFile = file
                    }
                    if (file.name.endsWith('.txt')) {
                        txtReportFile = file
                    }
                }

                if (xmlReportFile == null) {
                    throw new GradleException(XML_REPORT_NOT_ENABLED)
                }
            } catch (Exception exception) {
                handleException(KTLINT_CONFIGURATION_ERROR, exception)
            }
            task.xmlReportFile = xmlReportFile
            task.htmlReportFile = txtReportFile
            task.violations = violations
            task.dependsOn ktlintTask
        }
    }
}
