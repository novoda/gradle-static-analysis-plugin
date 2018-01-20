package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.Violations
import com.novoda.staticanalysis.internal.checkstyle.CheckstyleConfigurator
import com.novoda.staticanalysis.internal.findbugs.FindbugsConfigurator
import com.novoda.staticanalysis.internal.pmd.PmdConfigurator
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

class StaticAnalysisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        NamedDomainObjectContainer<Violations> violationsContainer = project.container(Violations)
        EvaluateViolationsTask evaluateViolations = createEvaluateViolationsTask(project, violationsContainer)
        createConfigurators(project, violationsContainer, evaluateViolations).each { configurator -> configurator.execute() }
        project.afterEvaluate {
            project.tasks['check'].dependsOn evaluateViolations
        }
    }

    private static EvaluateViolationsTask createEvaluateViolationsTask(Project project,
                                                                       NamedDomainObjectContainer<Violations> violationsContainer) {
        StaticAnalysisExtension extension = project.extensions.create('staticAnalysis', StaticAnalysisExtension, project)
        project.tasks.create('evaluateViolations', EvaluateViolationsTask) { task ->
            task.penaltyExtension = extension.penalty
            task.violationsContainer = violationsContainer
            task.conventionMapping.putAt('reportUrlRenderer', { extension.reportUrlRenderer })
        }
    }

    private static List<CodeQualityConfigurator> createConfigurators(Project project,
                                                                     NamedDomainObjectContainer<Violations> violationsContainer,
                                                                     EvaluateViolationsTask evaluateViolationsTask) {
        [
                CheckstyleConfigurator.create(project, violationsContainer, evaluateViolationsTask),
                new PmdConfigurator(project, evaluateViolationsTask),
                new FindbugsConfigurator(project, evaluateViolationsTask)
        ]
    }
}
