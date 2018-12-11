package com.novoda.staticanalysis

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

class DefaultViolationsEvaluator implements ViolationsEvaluator {

    private final ReportUrlRenderer reportUrlRenderer
    private final Logger logger
    private final PenaltyExtension penalty

    DefaultViolationsEvaluator(ReportUrlRenderer reportUrlRenderer, Logger logger, PenaltyExtension penalty) {
        this.reportUrlRenderer = reportUrlRenderer
        this.logger = logger
        this.penalty = penalty
    }

    @Override
    void evaluate(Set<Violations> allViolations) {
        String fullMessage = ''
        allViolations.each { Violations violations ->
            if (!violations.isEmpty()) {
                fullMessage += "> ${getViolationsMessage(violations, reportUrlRenderer)}\n"
            }
        }
        int totalErrors = allViolations.collect { it.errors }.sum() as int
        int totalWarnings = allViolations.collect { it.warnings }.sum() as int

        int errorsDiff = Math.max(0, totalErrors - penalty.maxErrors)
        int warningsDiff = Math.max(0, totalWarnings - penalty.maxWarnings)
        if (errorsDiff > 0 || warningsDiff > 0) {
            throw new GradleException("Violations limit exceeded by $errorsDiff errors, $warningsDiff warnings.\n\n$fullMessage")
        } else if (!fullMessage.isEmpty()) {
            logger.warn "\nViolations found ($totalErrors errors, $totalWarnings warnings)\n\n$fullMessage"
        }
    }

    private static String getViolationsMessage(Violations violations, ReportUrlRenderer reportUrlRenderer) {
        "$violations.name violations found ($violations.errors errors, $violations.warnings warnings). See the reports at:\n" +
                "${violations.reports.collect { "- ${reportUrlRenderer.render(it)}" }.join('\n')}"
    }
}
