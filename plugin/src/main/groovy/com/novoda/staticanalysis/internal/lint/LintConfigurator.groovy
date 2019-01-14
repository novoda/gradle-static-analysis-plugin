package com.novoda.staticanalysis.internal.lint

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantFilter
import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import static com.novoda.staticanalysis.internal.TasksCompat.maybeCreateTask

class LintConfigurator implements Configurator {

    private final Project project
    private final Violations violations
    private final VariantFilter variantFilter

    static LintConfigurator create(Project project, NamedDomainObjectContainer<Violations> violationsContainer) {
        Violations violations = violationsContainer.maybeCreate('Lint')
        return new LintConfigurator(project, violations)
    }

    private LintConfigurator(Project project, Violations violations) {
        this.project = project
        this.violations = violations
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
        if (variantFilter.includeVariantsFilter != null) {
            variants.all {
                configureCollectViolationsTask(it.name, "lint-results-${it.name}")
            }
        } else {
            configureCollectViolationsTask('lint-results')
        }
    }

    private void configureCollectViolationsTask(String taskSuffix = '', String reportFileName) {
        maybeCreateTask(project, "collectLint${taskSuffix.capitalize()}Violations", CollectLintViolationsTask) { task ->
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
