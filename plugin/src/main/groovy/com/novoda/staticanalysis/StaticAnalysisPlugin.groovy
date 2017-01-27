package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.Violations
import com.novoda.staticanalysis.internal.checkstyle.CheckstyleConfigurator
import com.novoda.staticanalysis.internal.findbugs.FindbugsConfigurator
import com.novoda.staticanalysis.internal.pmd.PmdConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.TemporaryFileProvider

import javax.inject.Inject

public class StaticAnalysisPlugin implements Plugin<Project> {

    private final TemporaryFileProvider temporaryFileProvider

    @Inject
    public StaticAnalysisPlugin(TemporaryFileProvider temporaryFileProvider) {
        this.temporaryFileProvider = temporaryFileProvider
    }

    @Override
    void apply(Project project) {
        EvaluateViolationsTask evaluateViolations = createEvaluateViolationsTask(project)
        createConfigurators(project, evaluateViolations).each { configurator -> configurator.execute() }
        project.afterEvaluate {
            project.tasks['check'].dependsOn evaluateViolations
        }
    }

    private EvaluateViolationsTask createEvaluateViolationsTask(Project project) {
        StaticAnalysisExtension extension = project.extensions.create('staticAnalysis', StaticAnalysisExtension)
        project.tasks.create('evaluateViolations', EvaluateViolationsTask) { task ->
            task.penaltyExtension = extension.penalty
            task.violationsContainer = project.container(Violations)
        }
    }

    private List<CodeQualityConfigurator> createConfigurators(Project project, EvaluateViolationsTask evaluateViolations) {
        [
                new CheckstyleConfigurator(project, evaluateViolations, temporaryFileProvider),
                new PmdConfigurator(project, evaluateViolations),
                new FindbugsConfigurator(project, evaluateViolations)
        ]
    }
}
