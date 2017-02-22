package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.StaticAnalysisExtension
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask

abstract class CodeQualityConfigurator<T extends SourceTask, E extends CodeQualityExtension> {

    protected final Project project
    protected final Violations violations
    protected final EvaluateViolationsTask evaluateViolations
    protected final SourceFilter sourceFilter
    protected Closure<Boolean> includeVariantsFilter

    protected CodeQualityConfigurator(Project project, Violations violations, EvaluateViolationsTask evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
        this.sourceFilter = new SourceFilter(project)
        this.includeVariantsFilter = { true }
    }

    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."$toolName" = { Closure config ->
            project.apply plugin: toolPlugin
            project.extensions.findByType(extensionClass).with {
                defaultConfiguration.execute(it)
                ext.exclude = { Object rule -> sourceFilter.exclude(rule) }
                ext.includeVariants = { Closure<Boolean> filter -> includeVariantsFilter = filter }
                config.delegate = it
                config()
            }
            project.afterEvaluate {
                boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
                boolean isAndroidLib = project.plugins.hasPlugin('com.android.library')
                if (isAndroidApp || isAndroidLib) {
                    NamedDomainObjectSet<Object> variants = project.container(Object)
                    variants.addAll(isAndroidApp ? project.android.applicationVariants : project.android.libraryVariants)
                    variants.addAll(project.android.testVariants)
                    variants.addAll(project.android.unitTestVariants)
                    configureAndroidProject(variants.matching { includeVariantsFilter(it) })
                } else {
                    configureJavaProject()
                }
                project.tasks.withType(taskClass) { task ->
                    task.group = 'verification'
                    configureReportEvaluation(task)
                }
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

    protected abstract void configureAndroidProject(NamedDomainObjectSet variants)

    protected void configureJavaProject() {
        project.tasks.withType(taskClass) { task -> sourceFilter.applyTo(task) }
    }

    protected abstract Class<T> getTaskClass()

    protected abstract void configureReportEvaluation(T task)

}
