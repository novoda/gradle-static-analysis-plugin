package com.novoda.staticanalysis

import com.novoda.staticanalysis.internal.Violations

interface ViolationsEvaluator {

    void evaluate(Set<Violations> allViolations)
}
