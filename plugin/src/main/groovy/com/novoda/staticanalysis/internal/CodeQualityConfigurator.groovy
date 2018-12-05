package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask

abstract class CodeQualityConfigurator<T extends SourceTask, E extends CodeQualityExtension> implements Configurator {

    protected final Project project
    protected final Violations violations
    protected final Task evaluateViolations
    protected final SourceFilter sourceFilter
    protected final VariantFilter variantFilter

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
                configureAndroidWithVariants(variantFilter.filteredApplicationVariants)
                configureToolTasks()
            }
            project.plugins.withId('com.android.library') {
                configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
                configureToolTasks()
            }
            project.plugins.withId('java') {
                configureJavaProject()
                configureToolTasks()
            }
        }
    }

    def configureAndroidWithVariants(DomainObjectSet variants) {
        variants.all { configureAndroidVariant(it) }
        variantFilter.filteredTestVariants.all { configureAndroidVariant(it) }
        variantFilter.filteredUnitTestVariants.all { configureAndroidVariant(it) }
    }

    def configureToolTasks() {
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
        return { ignored ->
            // no op
        }
    }

    protected abstract void configureAndroidVariant(variant)

    protected void configureJavaProject() {
        project.tasks.withType(taskClass) { task ->
            sourceFilter.applyTo(task)
            task.exclude '**/*.kt'
        }
    }

    protected abstract Class<T> getTaskClass()

    protected abstract void configureReportEvaluation(T task, Violations violations)

}
