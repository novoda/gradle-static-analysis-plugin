package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

class PmdConfigurator extends CodeQualityConfigurator<Pmd, PmdExtension> {

    static PmdConfigurator create(Project project,
                                  NamedDomainObjectContainer<Violations> violationsContainer,
                                  EvaluateViolationsTask evaluateViolationsTask) {
        Violations violations = violationsContainer.maybeCreate('PMD')
        return new PmdConfigurator(project, violations, evaluateViolationsTask)
    }

    private PmdConfigurator(Project project, Violations violations, EvaluateViolationsTask evaluateViolationsTask) {
        super(project, violations, evaluateViolationsTask)
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
        new Action<PmdExtension>() {
            @Override
            void execute(PmdExtension pmdExtension) {
                pmdExtension.toolVersion = '5.5.1'
                pmdExtension.rulePriority = 5
            }
        }
    }

    @Override
    protected void configureAndroidProject(NamedDomainObjectSet variants) {
        project.with {
            variants.all { variant ->
                variant.sourceSets.each { sourceSet ->
                    def taskName = "pmd${sourceSet.name.capitalize()}"
                    Pmd task = tasks.findByName(taskName)
                    if (task == null) {
                        task = tasks.create(taskName, Pmd)
                        task.with {
                            description = "Run PMD analysis for ${sourceSet.name} classes"
                            source = sourceSet.java.srcDirs
                        }
                    }
                    sourceFilter.applyTo(task)
                    task.mustRunAfter variant.javaCompile
                }
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Pmd pmd, Violations violations) {
        pmd.ignoreFailures = true
        pmd.metaClass.getLogger = { QuietLogger.INSTANCE }

        def collectViolations = createViolationsCollectionTask(pmd, violations)

        evaluateViolations.dependsOn collectViolations
        collectViolations.dependsOn pmd
    }

    private CollectPmdViolationsTask createViolationsCollectionTask(Pmd pmd, Violations violations) {
        project.tasks.create("collect${pmd.name.capitalize()}Violations", CollectPmdViolationsTask) { collectViolations ->
            collectViolations.xmlReportFile = pmd.reports.xml.destination
            collectViolations.violations = violations
        }
    }

}
