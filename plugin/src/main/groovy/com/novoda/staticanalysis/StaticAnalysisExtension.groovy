package com.novoda.staticanalysis

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

class StaticAnalysisExtension {

    final Action<? super PenaltyExtension> none = {
        it.maxWarnings = Integer.MAX_VALUE
        it.maxErrors = Integer.MAX_VALUE
    }

    final Action<? super PenaltyExtension> failOnErrors = {
        it.maxWarnings = Integer.MAX_VALUE
        it.maxErrors = 0
    }

    final Action<? super PenaltyExtension> failOnWarnings = {
        it.maxWarnings = 0
        it.maxErrors = 0
    }

    private PenaltyExtension currentPenalty = new PenaltyExtension()
    private final LogsExtension logs
    private final NamedDomainObjectContainer<RulesExtension> rules

    StaticAnalysisExtension(Project project) {
        this.logs = new LogsExtension(project)
        this.rules = project.container(RulesExtension, new NamedDomainObjectFactory<RulesExtension>() {
            @Override
            RulesExtension create(String name) {
                new RulesExtension(name, project)
            }
        })
    }

    void penalty(Action<? super PenaltyExtension> action) {
        action.execute(currentPenalty)
    }

    void logs(Action<? super LogsExtension> action) {
        action.execute(logs)
    }

    void rules(Action<? super NamedDomainObjectContainer<RulesExtension>> action) {
        action.execute(rules)
    }

    PenaltyExtension getPenalty() {
        currentPenalty
    }

    ReportUrlRenderer getReportUrlRenderer() {
        logs.reportUrlRenderer
    }

    NamedDomainObjectContainer<RulesExtension> getRules() {
        rules
    }

}
