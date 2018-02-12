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
                if (includeVariantsFilter != null) {
                    filteredApplicationVariants.all { configureCollectViolationsTask(it) }
                } else {
                    configureCollectViolationsTask()
                }
            }
            project.plugins.withId('com.android.library') {
                configureLint(config)
                if (includeVariantsFilter != null) {
                    filteredLibraryVariants.all { configureCollectViolationsTask(it) }
                } else {
                    configureCollectViolationsTask()
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

    private void configureCollectViolationsTask(variant = null) {
        def taskSuffix = variant ? variant.name : ''
        def collectViolations = createCollectViolationsTask(taskSuffix, violations).with {
            it.dependsOn project.tasks.findByName("lint${taskSuffix.capitalize()}")
        }
        evaluateViolations.dependsOn collectViolations
    }

    private CollectLintViolationsTask createCollectViolationsTask(String taskSuffix, Violations violations) {
        project.tasks.create("collectLint${taskSuffix.capitalize()}Violations", CollectLintViolationsTask) { task ->
            def reportSuffix = taskSuffix ? "-$taskSuffix" : ''
            task.xmlReportFile = xmlOutputFileFor(reportSuffix)
            task.htmlReportFile = new File(defaultOutputFolder, "lint-results${reportSuffix}.html")
            task.violations = violations
        }
    }

    private File xmlOutputFileFor(reportSuffix) {
        project.android.lintOptions.xmlOutput ?: new File(defaultOutputFolder, "lint-results${reportSuffix}.xml")
    }

    private File getDefaultOutputFolder() {
        new File(project.buildDir, 'reports')
    }

}
