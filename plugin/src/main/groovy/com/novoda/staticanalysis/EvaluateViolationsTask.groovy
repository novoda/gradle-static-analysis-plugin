package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

class EvaluateViolationsTask extends DefaultTask {
    private PenaltyExtension penaltyExtension
    private NamedDomainObjectContainer<Violations> violationsContainer
    private ReportUrlRenderer reportUrlRenderer

    EvaluateViolationsTask() {
        group = 'verification'
        description = 'Evaluate total violations against penaltyExtension thresholds.'
    }

    void setPenaltyExtension(PenaltyExtension penalty) {
        this.penaltyExtension = penalty
    }

    void setViolationsContainer(NamedDomainObjectContainer<Violations> allViolations) {
        this.violationsContainer = allViolations
    }

    Violations maybeCreate(String name) {
        violationsContainer.maybeCreate(name)
    }

    ReportUrlRenderer getReportUrlRenderer() {
        reportUrlRenderer
    }

    @TaskAction
    void run() {
        Map<String, Integer> total = [errors: 0, warnings: 0]
        String fullMessage = '\n'
        violationsContainer.each { Violations violations ->
            int errors = violations.errors
            int warnings = violations.warnings
            if (errors > 0 || warnings > 0) {
                fullMessage += "> ${getViolationsMessage(violations)}\n"
                total['errors'] += errors
                total['warnings'] += warnings
            }
        }
        int errorsDiff = Math.max(0, total['errors'] - penaltyExtension.maxErrors)
        int warningsDiff = Math.max(0, total['warnings'] - penaltyExtension.maxWarnings)
        if (errorsDiff > 0 || warningsDiff > 0) {
            throw new GradleException("Violations limit exceeded by $errorsDiff errors, $warningsDiff warnings.\n$fullMessage")
        } else {
            project.logger.warn fullMessage
        }
    }

    String getViolationsMessage(Violations violations) {
        "$violations.name rule violations were found ($violations.errors errors, $violations.warnings warnings). See the reports at:\n" +
                "${violations.reports.collect { "- ${reportUrlRenderer.render(it)}" }.join('\n')}"
    }

}
