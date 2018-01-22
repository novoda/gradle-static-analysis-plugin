package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations

interface ViolationsEvaluator {

    void evaluate(Input input)

    static class Input {

        private final PenaltyExtension penalty
        private final Set<Violations> allViolations

        Input(PenaltyExtension penalty, Set<Violations> allViolations) {
            this.penalty = penalty
            this.allViolations = allViolations
        }

        Input(PenaltyExtension penalty, Violations... allViolations) {
            this.penalty = penalty
            this.allViolations = allViolations as Set
        }

        PenaltyExtension getPenalty() {
            penalty
        }

        Set<Violations> getAllViolations() {
            allViolations
        }
    }
}
