package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations

interface ViolationsEvaluator {

    void evaluate(Input input)

    static class Input {

        private final PenaltyExtension penalty
        private final Violations[] allViolations

        Input(PenaltyExtension penalty, Set<Violations> allViolations) {
            this.penalty = penalty
            this.allViolations = allViolations as List
        }

        Input(PenaltyExtension penalty, Violations... allViolations) {
            this.penalty = penalty
            this.allViolations = allViolations
        }

        PenaltyExtension getPenalty() {
            penalty
        }

        Violations[] getAllViolations() {
            allViolations
        }
    }
}
