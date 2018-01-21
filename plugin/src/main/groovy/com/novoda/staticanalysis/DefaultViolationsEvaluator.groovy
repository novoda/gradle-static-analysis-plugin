package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.GradleException

class DefaultViolationsEvaluator implements ViolationsEvaluator {

    @Override
    void evaluate(StaticAnalysisExtension pluginExtension) {
        PenaltyExtension penaltyExtension = pluginExtension.penalty
        ReportUrlRenderer reportUrlRenderer = pluginExtension.reportUrlRenderer
        Map<String, Integer> total = [errors: 0, warnings: 0]
        String fullMessage = '\n'
        pluginExtension.allViolations.each { Violations violations ->
            int errors = violations.errors
            int warnings = violations.warnings
            if (errors > 0 || warnings > 0) {
                fullMessage += "> ${getViolationsMessage(violations, reportUrlRenderer)}\n"
                total['errors'] += errors
                total['warnings'] += warnings
            }
        }
        int errorsDiff = Math.max(0, total['errors'] - penaltyExtension.maxErrors)
        int warningsDiff = Math.max(0, total['warnings'] - penaltyExtension.maxWarnings)
        if (errorsDiff > 0 || warningsDiff > 0) {
            throw new GradleException("Violations limit exceeded by $errorsDiff errors, $warningsDiff warnings.\n$fullMessage")
        } else {
            pluginExtension.logger.warn fullMessage
        }
    }

    private static String getViolationsMessage(Violations violations, ReportUrlRenderer reportUrlRenderer) {
        "$violations.name violations found ($violations.errors errors, $violations.warnings warnings). See the reports at:\n" +
                "${violations.reports.collect { "- ${reportUrlRenderer.render(it)}" }.join('\n')}"
    }
}
