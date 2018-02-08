package com.novoda.staticanalysis.internal.lint

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantAware
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

class LintConfigurator implements Configurator, VariantAware {

    private final Project project
    private final Violations violations
    private final Task evaluateViolations

    static LintConfigurator create(Project project,
                                   NamedDomainObjectContainer<Violations> violationsContainer,
                                   Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('Lint')
        return new LintConfigurator(project, violations, evaluateViolations)
    }

    private LintConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.lintOptions = { Closure config ->
            project.plugins.withId('com.android.application') {
                configureLint(config)
                filteredApplicationVariants.all {
                    configureCollectViolationsTask(it)
                }
            }
            project.plugins.withId('com.android.library') {
                configureLint(config)
                filteredLibraryVariants.all {
                    configureCollectViolationsTask(it)
                }
            }

        }
    }

    private void configureLint(Closure config) {
        project.android.lintOptions.ext.includeVariants = { Closure<Boolean> filter ->
            includeVariantsFilter = filter
        }
        project.android.lintOptions(config)
        project.android.lintOptions {
            xmlReport = true
            htmlReport = true
            abortOnError false
        }
    }

    private void configureCollectViolationsTask(variant) {
        def collectViolations = createCollectViolationsTask(variant, violations)
        evaluateViolations.dependsOn collectViolations
        collectViolations.dependsOn project.tasks["lint${variant.name.capitalize()}"]
    }

    private CollectLintViolationsTask createCollectViolationsTask(variant, Violations violations) {
        project.tasks.create("collectLint${variant.name.capitalize()}Violations", CollectLintViolationsTask) { task ->
            task.xmlReportFile = xmlOutputFileFor(variant)
            task.htmlReportFile = new File(defaultOutputFolder, "lint-results-${variant.name}.html")
            task.violations = violations
        }
    }

    private File xmlOutputFileFor(variant) {
        project.android.lintOptions.xmlOutput ?: new File(defaultOutputFolder, "lint-results-${variant.name}.xml")
    }

    private File getDefaultOutputFolder() {
        new File(project.buildDir, 'reports')
    }

}
