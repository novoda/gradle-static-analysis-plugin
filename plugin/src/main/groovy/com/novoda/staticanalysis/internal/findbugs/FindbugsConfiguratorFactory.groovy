package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.Violations
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task

@Deprecated
class FindbugsConfiguratorFactory {

    private FindbugsConfiguratorFactory() {
    }

    static FindbugsConfigurator create(Project project,
                                       NamedDomainObjectContainer<Violations> violationsContainer,
                                       Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('Findbugs')
        return new FindbugsConfigurator(project, violations, evaluateViolations)
    }
}
