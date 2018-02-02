package com.novoda.staticanalysis

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class EvaluateViolationsTask extends DefaultTask {

    Closure<ViolationsEvaluator> evaluator
    Closure<Set<Violations>> allViolations

    EvaluateViolationsTask() {
        group = 'verification'
        description = 'Evaluate total violations against penaltyExtension thresholds.'
    }


    @TaskAction
    void run() {
        evaluator().evaluate(allViolations())
    }
}
