package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.StaticAnalysisExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask

abstract class CodeQualityConfigurator<T extends SourceTask, E extends CodeQualityExtension> {

    protected final Project project
    protected final Violations violations
    protected final EvaluateViolationsTask evaluateViolations
    protected final SourceFilter filter = new SourceFilter()

    protected CodeQualityConfigurator(Project project, Violations violations, EvaluateViolationsTask evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."$toolName" = { Closure config ->
            project.apply plugin: toolPlugin
            project.extensions.findByType(extensionClass).with {
                defaultConfiguration.execute(it)
                ext.exclude = { Object rule -> filter.exclude(rule) }
                config.delegate = it
                config()
            }
            project.afterEvaluate {
                boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
                boolean isAndroidLib = project.plugins.hasPlugin('com.android.library')
                if (isAndroidApp || isAndroidLib) {
                    configureAndroid(isAndroidApp ? project.android.applicationVariants : project.android.libraryVariants)
                }
                project.tasks.withType(taskClass) { task -> configureTask(task) }
            }
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

    protected abstract void configureAndroid(Object variants)

    protected abstract Class<T> getTaskClass()

    protected void configureTask(T task) {
        task.group = 'verification'
        filter.applyTo(task)
    }

}
