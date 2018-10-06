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

class KtlintConfigurator implements Configurator {

    private static final String KTLINT_PLUGIN = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_NOT_APPLIED = 'The Ktlint plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JLLeitschuh/ktlint-gradle/#how-to-use'

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

            def ktlint = project.ktlint
            ktlint.ignoreFailures = true
            ktlint.reporters = ['CHECKSTYLE', 'PLAIN']
            ktlint.ext.includeVariants = { Closure<Boolean> filter ->
                variantFilter.includeVariantsFilter = filter
            }
            config.delegate = ktlint
            config()

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
                project.plugins.withId('com.android.application') {
                    configureAndroidWithVariants(variantFilter.filteredApplicationVariants)
                }
                project.plugins.withId('com.android.library') {
                    configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
                }
            }
        }
    }

    private void configureKotlinProject() {
        project.sourceSets.each { configureKtlint(it) }
    }

    private void configureAndroidWithVariants(def mainVariants) {
        mainVariants.all { configureKtlint(it) }
        variantFilter.filteredTestVariants.all { configureKtlint(it) }
        variantFilter.filteredUnitTestVariants.all { configureKtlint(it) }
    }

    private void configureKtlint(def sourceSet) {
        def collectViolations = createCollectViolationsTask(violations, sourceSet.name)
        evaluateViolations.dependsOn collectViolations
    }

    private def createCollectViolationsTask(Violations violations, def sourceSetName) {
        project.tasks.create("collectKtlint${sourceSetName.capitalize()}Violations", CollectCheckstyleViolationsTask) { task ->
            task.xmlReportFile = new File(project.buildDir, "reports/ktlint/ktlint-${sourceSetName}.xml")
            task.htmlReportFile = new File(project.buildDir, "reports/ktlint/ktlint-${sourceSetName}.txt")
            task.violations = violations
            task.dependsOn project.tasks["ktlint${sourceSetName.capitalize()}Check"]
        }
    }
}
