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

class StaticAnalysisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def pluginExtension = project.extensions.create('staticAnalysis', StaticAnalysisExtension, project)
        def evaluateViolations = createEvaluateViolationsTask(project, pluginExtension)
        createConfigurators(project, pluginExtension, evaluateViolations).each { configurator -> configurator.execute() }
        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn evaluateViolations
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
                CheckstyleConfigurator.create(project, violationsContainer, evaluateViolations),
                PmdConfigurator.create(project, violationsContainer, evaluateViolations),
                FindbugsConfigurator.create(project, violationsContainer, evaluateViolations),
                DetektConfigurator.create(project, violationsContainer, evaluateViolations),
                KtlintConfigurator.create(project, violationsContainer, evaluateViolations),
                LintConfigurator.create(project, violationsContainer, evaluateViolations)
        ]
    }
}
