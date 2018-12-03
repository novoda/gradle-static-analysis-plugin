package com.novoda.staticanalysis.internal.idea

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

class IdeaConfigurator implements Configurator {

    private static final String IDEA_PLUGIN = 'org.jetbrains.intellij.inspections'
    private static final String IDEA_NOT_APPLIED = 'The Idea Inspections plugin is configured but not applied. Please apply the plugin in your build script.\nFor more information see https://github.com/JetBrains/inspection-plugin'

    private final Project project
    private final Violations violations
    private final Task evaluateViolations

    static IdeaConfigurator create(Project project,
                                   NamedDomainObjectContainer<Violations> violationsContainer,
                                   Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('inspections')
        return new IdeaConfigurator(project, violations, evaluateViolations)
    }

    IdeaConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.inspections = { Closure config ->
            if (!project.plugins.hasPlugin(IDEA_PLUGIN)) {
                throw new GradleException(IDEA_NOT_APPLIED)
            }
        }
    }
}
