package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.CollectViolationsTask
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

import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class StaticAnalysisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def pluginExtension = project.extensions.create('staticAnalysis', StaticAnalysisExtension, project)
        def evaluateViolations = createEvaluateViolationsTask(project, pluginExtension)
        createConfigurators(project, pluginExtension).each { configurator -> configurator.execute() }
        project.afterEvaluate {
            project.tasks['check'].dependsOn evaluateViolations
        }

    }

    private static def createEvaluateViolationsTask(Project project, StaticAnalysisExtension extension) {
        createTask(project, 'evaluateViolations', EvaluateViolationsTask) { task ->
            task.evaluator = { extension.evaluator }
            task.allViolations = { extension.allViolations }

            project.tasks.withType(ToolTriggerTask) { toolTrigger ->
                task.dependsOn(toolTrigger)
            }
            project.tasks.withType(CollectViolationsTask) { collectViolations ->
                task.dependsOn(collectViolations)
            }
        }
    }

    private static List<Configurator> createConfigurators(Project project, StaticAnalysisExtension pluginExtension) {
        NamedDomainObjectContainer<Violations> violationsContainer = pluginExtension.allViolations
        return [
                CheckstyleConfigurator.create(project, violationsContainer),
                PmdConfigurator.create(project, violationsContainer),
                FindbugsConfigurator.create(project, violationsContainer),
                DetektConfigurator.create(project, violationsContainer),
                KtlintConfigurator.create(project, violationsContainer),
                LintConfigurator.create(project, violationsContainer)
        ]
    }
}
