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

        Violations checkstyleViolations = violationsContainer.maybeCreate('Checkstyle')
        Violations pmdViolations = violationsContainer.maybeCreate('PMD')
        Violations findBugsViolations = violationsContainer.maybeCreate('Findbugs')
        Violations detektViolations = violationsContainer.maybeCreate('Detekt')
        Violations ktLintViolations = violationsContainer.maybeCreate('ktlint')
        Violations lintViolations = violationsContainer.maybeCreate('Lint')

        List<Task> violationsTasks = [
                createTask('evaluateCheckstyleViolations', project, pluginExtension, checkstyleViolations),
                createTask('evaluatePMDViolations', project, pluginExtension, pmdViolations),
                createTask('evaluateFindbugsViolations', project, pluginExtension, findBugsViolations),
                createTask('evaluateDetektViolations', project, pluginExtension, detektViolations),
                createTask('evaluateKtLintViolations', project, pluginExtension, ktLintViolations),
                createTask('evaluateLintViolations', project, pluginExtension, lintViolations)
        ].each { task -> evaluateViolations.dependsOn(task) }

        def configuratorList = [
                CheckstyleConfigurator.create(project, checkstyleViolations, violationsTasks.get(0)),
                PmdConfigurator.create(project, pmdViolations, violationsTasks.get(1)),
                FindbugsConfigurator.create(project, findBugsViolations, violationsTasks.get(2)),
                DetektConfigurator.create(project, detektViolations, violationsTasks.get(3)),
                KtlintConfigurator.create(project, ktLintViolations, violationsTasks.get(4)),
                LintConfigurator.create(project, lintViolations, violationsTasks.get(5))
        ]
        return configuratorList
    }

    private static Task createTask(String name,
                                   Project project,
                                   StaticAnalysisExtension extension,
                                   Violations violationsContainer
    ) {
        project.tasks.create(name, EvaluateToolViolationsTask) { task ->
            task.evaluator = { extension.evaluator }
            task.allViolations = { extension.allViolations }
            task.toolViolations = { violationsContainer } as Closure<Violations>
        }
    }
}
