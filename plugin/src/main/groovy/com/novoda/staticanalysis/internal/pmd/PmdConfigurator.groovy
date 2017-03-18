package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

class PmdConfigurator extends CodeQualityConfigurator<Pmd, PmdExtension> {

    PmdConfigurator(Project project, EvaluateViolationsTask evaluateViolations) {
        super(project, evaluateViolations.maybeCreate('PMD'), evaluateViolations)
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
        pmd.doLast {
            File xmlReportFile = pmd.reports.xml.destination
            File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')
            evaluateReports(xmlReportFile, htmlReportFile, violations)
        }
        evaluateViolations.dependsOn pmd
    }

    private static void evaluateReports(File xmlReportFile, File htmlReportFile, Violations violations) {
        PmdViolationsEvaluator evaluator = new PmdViolationsEvaluator(xmlReportFile)
        int errors = 0, warnings = 0
        evaluator.collectViolations().each { PmdViolationsEvaluator.PmdViolation violation ->
            if (violation.isError()) {
                errors += 1
            } else {
                warnings += 1
            }
        }
        violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
    }
}
