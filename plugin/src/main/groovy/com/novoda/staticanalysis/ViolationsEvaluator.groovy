package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations

interface ViolationsEvaluator {

    void evaluate(Input input)

    static class Input {

        private final PenaltyExtension penaltyExtension
        private final Violations[] allViolations

        Input(PenaltyExtension penaltyExtension, Violations... allViolations) {
            this.penaltyExtension = penaltyExtension
            this.allViolations = allViolations
        }

        PenaltyExtension getPenaltyExtension() {
            penaltyExtension
        }

        Violations[] getAllViolations() {
            allViolations
        }
    }
}
