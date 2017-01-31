package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.internal.logging.ConsoleRenderer

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
    protected void configureAndroid(Object variants) {
        project.with {
            variants.all { variant ->
                variant.sourceSets.each { sourceSet ->
                    def taskName = "pmd${sourceSet.name.capitalize()}"
                    Pmd pmd = tasks.findByName(taskName)
                    if (pmd == null) {
                        pmd = tasks.create(taskName, Pmd)
                        def sourceDirs = sourceSet.java.srcDirs
                        def notEmptyDirs = sourceDirs.findAll { it.list()?.length > 0 }
                        if (!notEmptyDirs.empty) {
                            pmd.with {
                                description = "Run PMD analysis for ${sourceSet.name} classes"
                                source = sourceSet.java.srcDirs
                            }
                        }
                    }
                    pmd.mustRunAfter variant.javaCompile
                }
            }
        }
    }

    @Override
    protected void configureTask(Pmd pmd) {
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
        String reportUrl = new ConsoleRenderer().asClickableFileUrl(htmlReportFile ?: xmlReportFile)
        violations.addViolations(errors, warnings, reportUrl)
    }
}
