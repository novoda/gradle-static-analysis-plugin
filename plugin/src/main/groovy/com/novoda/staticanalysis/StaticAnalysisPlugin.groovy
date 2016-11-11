package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import com.novoda.staticanalysis.internal.checkstyle.CheckstyleConfigurator
import com.novoda.staticanalysis.internal.findbugs.FindbugsConfigurator
import com.novoda.staticanalysis.internal.pmd.PmdConfigurator
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class StaticAnalysisPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        StaticAnalysisExtension extension = project.extensions.create('staticAnalysis', StaticAnalysisExtension)

        NamedDomainObjectContainer<Violations> allViolations = project.container(Violations)
        Task evaluateViolations = project.tasks.create('evaluateViolations', EvaluateViolationsTask) { task ->
            task.penalty = extension.penalty
            task.allViolations = allViolations
        }
        [
                new CheckstyleConfigurator(project, allViolations.create('Checkstyle'), evaluateViolations),
                new PmdConfigurator(project, allViolations.create('PMD'), evaluateViolations),
                new FindbugsConfigurator(project, allViolations.create('Findbugs'), evaluateViolations)
        ].each { configurator -> configurator.execute() }

        project.afterEvaluate {
            project.tasks['check'].dependsOn evaluateViolations
        }
    }

}
