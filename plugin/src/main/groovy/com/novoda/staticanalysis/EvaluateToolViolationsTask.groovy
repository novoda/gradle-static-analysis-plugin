package com.novoda.staticanalysis

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class EvaluateToolViolationsTask extends DefaultTask {

    Closure<ViolationsEvaluator> evaluator
    Closure<Set<Violations>> allViolations
    Violations toolViolations

    EvaluateToolViolationsTask() {
        group = 'verification'
        description = "Evaluate violations against penaltyExtension thresholds."
    }

    @TaskAction
    void run() {
        Set violationsSet = [toolViolations]
        evaluator().evaluate(violationsSet)
    }
}
