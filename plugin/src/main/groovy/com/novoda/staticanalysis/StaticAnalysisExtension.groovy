package com.novoda.staticanalysis

import org.gradle.api.Action
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

    StaticAnalysisExtension(Project project) {
        this.logs = new LogsExtension(project)
    }

    void penalty(Action<? super PenaltyExtension> action) {
        action.execute(currentPenalty)
    }

    void logs(Action<? super LogsExtension> action) {
        action.execute(logs)
    }

    PenaltyExtension getPenalty() {
        currentPenalty
    }

    ReportUrlRenderer getReportUrlRenderer() {
        logs.reportUrlRenderer
    }

}
