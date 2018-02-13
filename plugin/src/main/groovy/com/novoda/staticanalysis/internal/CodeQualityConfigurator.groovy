package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask

abstract class CodeQualityConfigurator<T extends SourceTask, E extends CodeQualityExtension> implements Configurator {

    protected final Project project
    protected final Violations violations
    protected final Task evaluateViolations
    protected final SourceFilter sourceFilter
    private final VariantFilter variantFilter

    protected CodeQualityConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
        this.sourceFilter = new SourceFilter(project)
        this.variantFilter = new VariantFilter(project)
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."$toolName" = { Closure config ->
            project.apply plugin: toolPlugin
            project.extensions.findByType(extensionClass).with {
                defaultConfiguration.execute(it)
                ext.exclude = { Object rule -> sourceFilter.exclude(rule) }
                ext.includeVariants = { Closure<Boolean> filter -> variantFilter.includeVariantsFilter = filter }
                config.delegate = it
                config()
            }
            project.plugins.withId('com.android.application') {
                project.afterEvaluate {
                    configureAndroidProject(variantFilter.filteredApplicationAndTestVariants)
                    configureToolTasks()
                }
            }
            project.plugins.withId('com.android.library') {
                project.afterEvaluate {
                    configureAndroidProject(variantFilter.filteredLibraryAndTestVariants)
                    configureToolTasks()
                }
            }
            project.plugins.withId('java') {
                project.afterEvaluate {
                    configureJavaProject()
                    configureToolTasks()
                }
            }
        }
    }

    protected void configureToolTasks() {
        project.tasks.withType(taskClass) { task ->
            task.group = 'verification'
            configureReportEvaluation(task, violations)
        }
    }

    protected abstract String getToolName()

    protected Object getToolPlugin() {
        toolName
    }

    protected abstract Class<E> getExtensionClass()

    protected Action<E> getDefaultConfiguration() {
        new Action<E>() {
            void execute(E ignored) {
                // no op
            }
        }
    }

    protected abstract void configureAndroidProject(NamedDomainObjectSet variants)

    protected void configureJavaProject() {
        project.tasks.withType(taskClass) { task -> sourceFilter.applyTo(task) }
    }

    protected abstract Class<T> getTaskClass()

    protected abstract void configureReportEvaluation(T task, Violations violations)

}
