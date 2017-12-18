package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

open class EvaluateViolationsTask : DefaultTask() {

    var penaltyExtension: PenaltyExtension? = null
    var violationsContainer: NamedDomainObjectContainer<Violations>? = null
    var reportUrlRenderer: ReportUrlRenderer? = ReportUrlRenderer.Default

    init {
        group = "verification"
        description = "Evaluate total violations against penaltyExtension thresholds."
    }

    fun maybeCreate(name: String): Violations? = violationsContainer?.maybeCreate(name)

    @TaskAction
    fun run() {
        val total = mutableMapOf("errors" to 0, "warnings" to 0)
        val fullMessage = StringBuilder("\n")

        violationsContainer?.forEach { violations ->
            val errors = violations.errors
            val warnings = violations.warnings
            if (errors > 0 || warnings > 0) {
                fullMessage.append("> ${violations.message()}\n")
                total["errors"] = total["errors"]!! + errors
                total["warnings"] = total["warnings"]!! + warnings
            }
        }

        val maxErrors = Math.max(penaltyExtension?.maxErrors ?: 0, 0)
        val maxWarnings = Math.max(penaltyExtension?.maxWarnings ?: 0, 0)

        val errorsDiff = Math.max(0, total["errors"]!! - maxErrors)
        val warningsDiff = Math.max(0, total["warnings"]!! - maxWarnings)

        if (errorsDiff > 0 || warningsDiff > 0) {
            throw GradleException("Violations limit exceeded by $errorsDiff errors, $warningsDiff warnings.\n$fullMessage")
        } else {
            project.logger.warn(fullMessage.toString())
        }
    }

    private fun Violations.message() =
        "${this.name} rule violations were found (${this.errors} errors, ${this.warnings} warnings). See the reports at:\n" +
                this.reports.joinToString(separator = "\n") { "- ${ReportUrlRenderer.Default.render(it)}" }

    // TODO obtain renderer from convention mappings
}
