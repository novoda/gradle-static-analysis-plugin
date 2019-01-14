package com.novoda.staticanalysis.internal.ktlint

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.ToolTriggerTask
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantFilter
import com.novoda.staticanalysis.internal.checkstyle.CollectCheckstyleViolationsTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

import static com.novoda.staticanalysis.internal.TasksCompat.*

class KtlintConfigurator implements Configurator {

    private static final String KTLINT_PLUGIN = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_NOT_APPLIED = 'The Ktlint plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JLLeitschuh/ktlint-gradle/#how-to-use'
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to add "CHECKSTYLE" into reports in your Ktlint configuration'

    private final Project project
    private final Violations violations
    private final VariantFilter variantFilter

    static KtlintConfigurator create(Project project, NamedDomainObjectContainer<Violations> violationsContainer) {
        Violations violations = violationsContainer.maybeCreate('ktlint')
        return new KtlintConfigurator(project, violations)
    }

    KtlintConfigurator(Project project, Violations violations) {
        this.project = project
        this.violations = violations
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
                createTask(project, 'triggerKtlint', ToolTriggerTask) { trigger ->
                    configureKtlint(trigger)
                }
            }
        }
    }

    private void configureKtlint(Task trigger) {
        project.plugins.withId("kotlin") {
            configureKotlinProject(trigger)
        }
        project.plugins.withId("kotlin2js") {
            configureKotlinProject(trigger)
        }
        project.plugins.withId("kotlin-platform-common") {
            configureKotlinProject(trigger)
        }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            configureKotlinProject(trigger)
        }
        project.plugins.withId('com.android.application') {
            configureAndroidWithVariants(trigger, variantFilter.filteredApplicationVariants)
        }
        project.plugins.withId('com.android.library') {
            configureAndroidWithVariants(trigger, variantFilter.filteredLibraryVariants)
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

    private void configureKotlinProject(Task trigger) {
        project.sourceSets.each { configureKtlint(trigger, it.name) }
    }

    private void configureAndroidWithVariants(Task trigger, def mainVariants) {
        mainVariants.all { configureAndroidVariant(trigger, it) }
        variantFilter.filteredTestVariants.all { configureAndroidVariant(trigger, it) }
        variantFilter.filteredUnitTestVariants.all { configureAndroidVariant(trigger, it) }
    }

    private void configureAndroidVariant(Task trigger, def variant) {
        variant.sourceSets.each { sourceSet ->
            configureKtlint(trigger, sourceSet.name)
        }
    }

    private void configureKtlint(Task trigger, String sourceSetName) {
        def tasks = project.tasks.matching {
            isKtlintTask(it, sourceSetName.capitalize())
        }
        configureEach(tasks) {
            configureKtlint(trigger, sourceSetName, it)
        }
    }

    private def configureKtlint(Task trigger, String sourceSetName, Task ktlintTask) {
        File xmlReportFile = null
        File txtReportFile = null
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
            throw new IllegalStateException(XML_REPORT_NOT_ENABLED)
        }
        def collectViolations = createCollectViolationsTask(violations, sourceSetName, ktlintTask, xmlReportFile, txtReportFile)
        trigger.dependsOn collectViolations
    }

    private def createCollectViolationsTask(Violations violations, def sourceSetName, Task ktlintTask, File xmlReportFile, File txtReportFile) {
        maybeCreateTask(project, "collectKtlint${sourceSetName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = xmlReportFile
            task.htmlReportFile = txtReportFile
            task.violations = violations
            task.dependsOn ktlintTask
        }
    }

    /**
     * KtLint task has the following naming convention and the needed property to resolve the reportOutputFiles
     */
    private static boolean isKtlintTask(Task task, String sourceSetName) {
        task.name ==~ /^ktlint$sourceSetName(SourceSet)?Check$/ && task.hasProperty('reportOutputFiles')
    }
}
