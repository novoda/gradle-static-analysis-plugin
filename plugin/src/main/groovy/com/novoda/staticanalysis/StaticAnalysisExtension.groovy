package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.logging.Logger

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

    private final Project project
    private final LogsExtension logs
    private final NamedDomainObjectContainer<Violations> allViolations
    private final NamedDomainObjectContainer<RulesExtension> rules
    private Closure<ViolationsEvaluator> createEvaluator

    StaticAnalysisExtension(Project project) {
        this.project = project
        this.logs = new LogsExtension(project)
        this.allViolations = project.container(Violations)
        this.rules = project.container(RulesExtension, new NamedDomainObjectFactory<RulesExtension>() {
            @Override
            RulesExtension create(String name) {
                new RulesExtension(name, project)
            }
        })
        this.createEvaluator = {
            new DefaultViolationsEvaluator(reportUrlRenderer, logger, new PenaltyExtension())
        }
    }

    void penalty(Action<? super PenaltyExtension> action) {
        this.createEvaluator = {
            PenaltyExtension penalty = new PenaltyExtension()
            action.execute(penalty)
            new DefaultViolationsEvaluator(reportUrlRenderer, logger, penalty)
        }
    }

    void logs(Action<? super LogsExtension> action) {
        action.execute(logs)
    }

    void rules(Action<? super NamedDomainObjectContainer<RulesExtension>> action) {
        action.execute(rules)
    }

    ReportUrlRenderer getReportUrlRenderer() {
        logs.reportUrlRenderer
    }

    NamedDomainObjectContainer<RulesExtension> getRules() {
        rules
    }

    NamedDomainObjectContainer<Violations> getAllViolations() {
        allViolations
    }

    ViolationsEvaluator getEvaluator() {
        createEvaluator()
    }

    Logger getLogger() {
        project.logger
    }

    void evaluator(Action<? super Set<Violations>> action) {
        createEvaluator = {
            new ViolationsEvaluator() {
                @Override
                void evaluate(Set<Violations> allViolations) {
                    action.execute(allViolations)
                }
            }
        }
    }
}
