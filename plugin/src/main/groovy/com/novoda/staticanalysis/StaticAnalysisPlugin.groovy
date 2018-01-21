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
        StaticAnalysisExtension pluginExtension = project.extensions.create('staticAnalysis', StaticAnalysisExtension, project)
        EvaluateViolationsTask evaluateViolationsTask = createEvaluateViolationsTask(project, pluginExtension)
        createConfigurators(project, pluginExtension, evaluateViolationsTask).each { configurator -> configurator.execute() }
        project.afterEvaluate {
            project.tasks['check'].dependsOn evaluateViolationsTask
        }
    }

    private static EvaluateViolationsTask createEvaluateViolationsTask(Project project,
                                                                       StaticAnalysisExtension extension) {
        project.tasks.create('evaluateViolations', EvaluateViolationsTask) { task ->
            task.penaltyExtension = extension.penalty
            task.violationsContainer = extension.allViolations
            task.conventionMapping.putAt('reportUrlRenderer', { extension.reportUrlRenderer })
        }
    }

    private static List<CodeQualityConfigurator> createConfigurators(Project project,
                                                                     StaticAnalysisExtension pluginExtension,
                                                                     EvaluateViolationsTask evaluateViolationsTask) {
        NamedDomainObjectContainer<Violations> violationsContainer = pluginExtension.allViolations
        [
                CheckstyleConfigurator.create(project, violationsContainer, evaluateViolationsTask),
                PmdConfigurator.create(project, violationsContainer, evaluateViolationsTask),
                FindbugsConfigurator.create(project, violationsContainer, evaluateViolationsTask)
        ]
    }
}
