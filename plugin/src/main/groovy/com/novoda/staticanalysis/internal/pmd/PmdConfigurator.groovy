package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

import static com.novoda.staticanalysis.internal.TasksCompat.maybeCreateTask

class PmdConfigurator extends CodeQualityConfigurator<Pmd, PmdExtension> {

    static PmdConfigurator create(Project project, NamedDomainObjectContainer<Violations> violationsContainer) {
        Violations violations = violationsContainer.maybeCreate('PMD')
        return new PmdConfigurator(project, violations)
    }

    private PmdConfigurator(Project project, Violations violations) {
        super(project, violations)
    }

    @Override
    protected String getToolName() {
        'pmd'
    }

    @Override
    protected Class<PmdExtension> getExtensionClass() {
        PmdExtension
    }

    @Override
    protected Class<Pmd> getTaskClass() {
        Pmd
    }

    @Override
    protected Action<PmdExtension> getDefaultConfiguration() {
        return { extension ->
            extension.toolVersion = '5.5.1'
            extension.rulePriority = 5
        }
    }

    @Override
    protected void configureAndroidVariant(variant) {
        variant.sourceSets.each { sourceSet ->
            def taskName = "pmd${sourceSet.name.capitalize()}"
            maybeCreateTask(project, taskName, Pmd) { Pmd task ->
                task.description = "Run PMD analysis for ${sourceSet.name} classes"
                task.source = sourceSet.java.srcDirs
                task.exclude '**/*.kt'
                sourceFilter.applyTo(task)
                task.mustRunAfter variant.javaCompile
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Pmd pmd, Violations violations) {
        pmd.ignoreFailures = true
        pmd.metaClass.getLogger = { QuietLogger.INSTANCE }

        createViolationsCollectionTask(pmd, violations)
    }

    private def createViolationsCollectionTask(Pmd pmd, Violations violations) {
        maybeCreateTask(project, "collect${pmd.name.capitalize()}Violations", CollectPmdViolationsTask) { task ->
            task.xmlReportFile = pmd.reports.xml.destination
            task.violations = violations
            task.dependsOn(pmd)
        }
    }
}
