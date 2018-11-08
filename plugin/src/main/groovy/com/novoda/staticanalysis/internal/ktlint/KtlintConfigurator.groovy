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
import org.gradle.api.file.RegularFileProperty

class KtlintConfigurator implements Configurator {

    private static final String KTLINT_PLUGIN = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_NOT_APPLIED = 'The Ktlint plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JLLeitschuh/ktlint-gradle/#how-to-use'
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to add "CHECKSTYLE" into reports in your Ktlint configuration'

    private final Project project
    private final Violations violations
    private final Task evaluateViolations
    private final VariantFilter variantFilter

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
                project.plugins.withId('com.android.application') {
                    configureAndroidWithVariants(variantFilter.filteredApplicationVariants)
                }
                project.plugins.withId('com.android.library') {
                    configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
                }
            }
        }
    }

    private void configureKtlintExtension(Closure config) {
        def ktlint = project.ktlint
        ktlint.ignoreFailures = true
        ktlint.ext.includeVariants = { Closure<Boolean> filter ->
            variantFilter.includeVariantsFilter = filter
        }
        config.delegate = ktlint
        config()
    }

    private void configureKotlinProject() {
        project.sourceSets.each { configureKtlint(it.name) }
    }

    private void configureAndroidWithVariants(def mainVariants) {
        mainVariants.all { configureKtlint(it.name) }
        variantFilter.filteredTestVariants.all { configureKtlint(it.name) }
        variantFilter.filteredUnitTestVariants.all { configureKtlint(it.name) }
    }

    private void configureKtlint(def sourceSetName) {
        project.tasks.matching {
            it.name == "ktlint${sourceSetName.capitalize()}Check"
        }.all { Task ktlintTask ->
            def collectViolations = configureKtlintWithOutputFiles(sourceSetName, ktlintTask.reportOutputFiles)
            collectViolations.dependsOn ktlintTask
            evaluateViolations.dependsOn collectViolations
        }
    }

    private def configureKtlintWithOutputFiles(def sourceSetName, Map<?, RegularFileProperty> reportOutputFiles) {
        File xmlReportFile = null
        File txtReportFile = null
        reportOutputFiles.each { key, fileProp ->
            def file = fileProp.get().asFile
            if (file.name.endsWith('.xml')) {
                xmlReportFile = file
            }
            if (file.name.endsWith('.txt')) {
                txtReportFile = file
            }
        }

        if (xmlReportFile == null) {
            throw new IllegalStateException(XML_REPORT_NOT_ENABLED)
        }

        createCollectViolationsTask(violations, sourceSetName, xmlReportFile, txtReportFile)
    }

    private def createCollectViolationsTask(Violations violations, def sourceSetName, File xmlReportFile, File txtReportFile) {
        project.tasks.create("collectKtlint${sourceSetName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = xmlReportFile
            task.htmlReportFile = txtReportFile
            task.violations = violations
        }
    }
}
