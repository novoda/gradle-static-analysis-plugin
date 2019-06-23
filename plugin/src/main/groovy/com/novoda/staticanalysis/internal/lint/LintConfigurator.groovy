package com.novoda.staticanalysis.internal.lint

import com.novoda.staticanalysis.EvaluateToolViolationsTask
import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantFilter
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task

import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class LintConfigurator implements Configurator {

    private final Project project
    private final Violations violations
    private final Task evaluateViolations
    private final VariantFilter variantFilter
    private boolean configured = false

    static LintConfigurator create(Project project,
                                   EvaluateToolViolationsTask evaluateViolations) {
        return new LintConfigurator(project, evaluateViolations.toolViolations, evaluateViolations)
    }

    private LintConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
        this.variantFilter = new VariantFilter(project)
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.lintOptions = { Closure config ->
            project.plugins.withId('com.android.application') {
                configureLint(config)
                configureWithVariants(variantFilter.filteredApplicationVariants)
            }
            project.plugins.withId('com.android.library') {
                configureLint(config)
                configureWithVariants(variantFilter.filteredLibraryVariants)
            }
        }
    }

    private void configureLint(Closure config) {
        project.android.lintOptions.ext.includeVariants = { Closure<Boolean> filter ->
            variantFilter.includeVariantsFilter = filter
        }
        project.android.lintOptions(config)
        project.android.lintOptions {
            xmlReport = true
            htmlReport = true
            abortOnError false
        }
    }

    private void configureWithVariants(DomainObjectSet variants) {
        if (configured) return

        if (variantFilter.includeVariantsFilter != null) {
            variants.all {
                configureCollectViolationsTask(it.name, "lint-results-${it.name}")
            }
        } else {
            configureCollectViolationsTask('lint-results')
        }
    }

    private void configureCollectViolationsTask(String taskSuffix = '', String reportFileName) {
        def collectViolations = createCollectViolationsTask(taskSuffix, reportFileName, violations)
        evaluateViolations.dependsOn collectViolations
        configured = true
    }

    private def createCollectViolationsTask(String taskSuffix, String reportFileName, Violations violations) {
        createTask(project, "collectLint${taskSuffix.capitalize()}Violations", CollectLintViolationsTask) { task ->
            task.xmlReportFile = xmlOutputFileFor(reportFileName)
            task.htmlReportFile = htmlOutputFileFor(reportFileName)
            task.violations = violations
            task.dependsOn project.tasks["lint${taskSuffix.capitalize()}"]
        }
    }

    private File xmlOutputFileFor(reportFileName) {
        project.android.lintOptions.xmlOutput ?: new File(defaultOutputFolder, "${reportFileName}.xml")
    }

    private File htmlOutputFileFor(reportFileName) {
        project.android.lintOptions.htmlOutput ?: new File(defaultOutputFolder, "${reportFileName}.html")
    }

    private File getDefaultOutputFolder() {
        new File(project.buildDir, 'reports')
    }

}
