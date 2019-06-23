package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.checkstyle.CheckstyleConfigurator
import com.novoda.staticanalysis.internal.detekt.DetektConfigurator
import com.novoda.staticanalysis.internal.findbugs.FindbugsConfigurator
import com.novoda.staticanalysis.internal.ktlint.KtlintConfigurator
import com.novoda.staticanalysis.internal.lint.LintConfigurator
import com.novoda.staticanalysis.internal.pmd.PmdConfigurator
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static com.novoda.staticanalysis.internal.TasksCompat.configureEach

class StaticAnalysisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def pluginExtension = project.extensions.create('staticAnalysis', StaticAnalysisExtension, project)
        def evaluateViolations = createEvaluateViolationsTask(project, pluginExtension)
        createConfigurators(project, pluginExtension).each { configurator -> configurator.execute() }
        configureEach(project.tasks.matching { it.name == 'check' }) { task ->
            task.dependsOn evaluateViolations
        }
    }

    private static Task createEvaluateViolationsTask(Project project,
                                                     StaticAnalysisExtension extension) {
        project.tasks.create('evaluateViolations', EvaluateViolationsTask) { task ->
            task.evaluator = { extension.evaluator }
            task.allViolations = { extension.allViolations }
        }
    }

    private static List<Configurator> createConfigurators(Project project, StaticAnalysisExtension pluginExtension) {
        NamedDomainObjectContainer<Violations> violationsContainer = pluginExtension.allViolations

        return [
                CheckstyleConfigurator.create(project, createTaskForTool('Checkstyle', project, pluginExtension, violationsContainer)),
                PmdConfigurator.create(project, createTaskForTool('PMD', project, pluginExtension, violationsContainer)),
                FindbugsConfigurator.create(project, createTaskForTool('Findbugs', project, pluginExtension, violationsContainer)),
                DetektConfigurator.create(project, createTaskForTool('Detekt', project, pluginExtension, violationsContainer)),
                KtlintConfigurator.create(project, createTaskForTool('ktlint', project, pluginExtension, violationsContainer)),
                LintConfigurator.create(project, createTaskForTool('Lint', project, pluginExtension, violationsContainer))
        ]
    }

    private static EvaluateToolViolationsTask createTaskForTool(
            String toolName,
            Project project,
            StaticAnalysisExtension extension,
            NamedDomainObjectContainer<Violations> violationsContainer
    ) {
        def task = project.tasks.create("evaluate${toolName}Violations", EvaluateToolViolationsTask) { task ->
            task.evaluator = { extension.evaluator }
            task.toolViolations = violationsContainer.maybeCreate(toolName)
        } as EvaluateToolViolationsTask

        project.tasks['evaluateViolations'].dependsOn task

        return task
    }
}
