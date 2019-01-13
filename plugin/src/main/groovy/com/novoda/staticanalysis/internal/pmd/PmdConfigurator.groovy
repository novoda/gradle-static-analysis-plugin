package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

import static com.novoda.staticanalysis.internal.TasksCompat.maybeCreateTask

class PmdConfigurator extends CodeQualityConfigurator<Pmd, PmdExtension> {

    static PmdConfigurator create(Project project,
                                  NamedDomainObjectContainer<Violations> violationsContainer,
                                  Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('PMD')
        return new PmdConfigurator(project, violations, evaluateViolations)
    }

    private PmdConfigurator(Project project,
                            Violations violations,
                            Task evaluateViolations) {
        super(project, violations, evaluateViolations)
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
        project.with {
            variant.sourceSets.each { sourceSet ->
                def taskName = "pmd${sourceSet.name.capitalize()}"
                Pmd task = tasks.findByName(taskName)
                if (task == null) {
                    task = tasks.create(taskName, Pmd)
                    task.with {
                        description = "Run PMD analysis for ${sourceSet.name} classes"
                        source = sourceSet.java.srcDirs
                        exclude '**/*.kt'
                    }
                }
                sourceFilter.applyTo(task)
                task.mustRunAfter variant.javaCompile
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Pmd pmd, Violations violations) {
        pmd.ignoreFailures = true
        pmd.metaClass.getLogger = { QuietLogger.INSTANCE }

        def collectViolations = createViolationsCollectionTask(pmd, violations)
        evaluateViolations.dependsOn collectViolations
    }

    private def createViolationsCollectionTask(Pmd pmd, Violations violations) {
        maybeCreateTask(project, "collect${pmd.name.capitalize()}Violations", CollectPmdViolationsTask) { task ->
            task.xmlReportFile = pmd.reports.xml.destination
            task.violations = violations
            task.dependsOn(pmd)
        }
    }
}
