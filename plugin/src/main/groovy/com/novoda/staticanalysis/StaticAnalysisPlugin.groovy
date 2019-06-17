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
        createConfigurators(project, pluginExtension, evaluateViolations).each { configurator -> configurator.execute() }
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

    private static List<Configurator> createConfigurators(Project project,
                                                          StaticAnalysisExtension pluginExtension,
                                                          Task evaluateViolations) {
        NamedDomainObjectContainer<Violations> violationsContainer = pluginExtension.allViolations
        [
                CheckstyleConfigurator.create(project, violationsContainer, createTask('evaluateCheckstyleViolations', project, pluginExtension, evaluateViolations)),
                PmdConfigurator.create(project, violationsContainer, createTask('evaluatePMDViolations', project, pluginExtension, evaluateViolations)),
                FindbugsConfigurator.create(project, violationsContainer, createTask('evaluateFindbugsViolations', project, pluginExtension, evaluateViolations)),
                DetektConfigurator.create(project, violationsContainer, createTask('evaluateDetektViolations', project, pluginExtension, evaluateViolations)),
                KtlintConfigurator.create(project, violationsContainer, createTask('evaluateKtLintViolations', project, pluginExtension, evaluateViolations)),
                LintConfigurator.create(project, violationsContainer, createTask('evaluateLintViolations', project, pluginExtension, evaluateViolations))
        ]
    }

    private static Task createTask(String name, Project project, StaticAnalysisExtension extension, Task evaluateViolations) {
        def task = project.tasks.create(name, EvaluateViolationsTask) { task ->
            task.evaluator = { extension.evaluator }
            task.allViolations = { extension.allViolations }
        }
        evaluateViolations.dependsOn.add(task)
        return task
    }
}
