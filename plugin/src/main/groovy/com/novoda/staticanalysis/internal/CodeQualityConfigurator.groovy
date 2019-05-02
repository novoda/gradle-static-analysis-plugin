package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.VerificationTask

import static com.novoda.staticanalysis.internal.TasksCompat.configureEach
import static com.novoda.staticanalysis.internal.TasksCompat.createTask

abstract class CodeQualityConfigurator<T extends SourceTask & VerificationTask, E extends CodeQualityExtension> implements Configurator {

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
            }
            project.plugins.withId('com.android.library') {
                configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
            }
            project.plugins.withId('java') {
                configureJavaProject()
            }
            configureEach(project.tasks.withType(taskClass)) { task ->
                configureToolTask(task)
            }
        }
    }

    def configureAndroidWithVariants(DomainObjectSet variants) {
        project.android.sourceSets.all { sourceSet ->
            createToolTaskForAndroid(sourceSet)
            createCollectViolations(getToolTaskNameFor(sourceSet), violations)
        }
        variants.all { configureVariant(it) }
        variantFilter.filteredTestVariants.all { configureVariant(it) }
        variantFilter.filteredUnitTestVariants.all { configureVariant(it) }
    }

    protected configureVariant(variant) {
        def collectViolations = createVariantMetaTask(variant)
        evaluateViolations.dependsOn collectViolations
    }

    private def createVariantMetaTask(variant) {
        createTask(project, "collectViolationsVariant${variant.name.capitalize()}", Task) { task ->
            task.group = 'verification'
            task.description = "Runs $toolName analysis on all sources for android ${variant.name} variant"
            task.mustRunAfter javaCompile(variant)

            variant.sourceSets.forEach { sourceSet ->
                def toolTaskName = getToolTaskNameFor(sourceSet)
                task.dependsOn "collect${toolTaskName.capitalize()}Violations"
            }
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

    protected abstract void createToolTaskForAndroid(sourceSet)

    private static def javaCompile(variant) {
        if (variant.hasProperty('javaCompileProvider')) {
            variant.javaCompileProvider
        } else {
            variant.javaCompile
        }
    }

    protected void configureJavaProject() {
        project.sourceSets.all { sourceSet ->
            def collectViolations = createCollectViolations(getToolTaskNameFor(sourceSet), violations)
            evaluateViolations.dependsOn collectViolations
        }
    }

    protected final String getToolTaskNameFor(sourceSet) {
        "$toolName${sourceSet.name.capitalize()}"
    }

    protected abstract Class<T> getTaskClass()

    protected void configureToolTask(T task) {
        sourceFilter.applyTo(task)
        task.group = 'verification'
        task.exclude '**/*.kt'
        task.ignoreFailures = true
        task.metaClass.getLogger = { QuietLogger.INSTANCE }
    }

    protected abstract def createCollectViolations(String taskName, Violations violations)
}
