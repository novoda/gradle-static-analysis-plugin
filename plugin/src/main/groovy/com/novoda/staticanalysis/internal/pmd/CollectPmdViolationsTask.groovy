package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.internal.CollectViolationsTask
import com.novoda.staticanalysis.internal.Violations

class CollectPmdViolationsTask extends CollectViolationsTask {

    @Override
    void collectViolations(File xmlReportFile, Violations violations) {
        File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')
        PmdViolationsEvaluator evaluator = new PmdViolationsEvaluator(xmlReportFile)
        int errors = 0, warnings = 0
        evaluator.collectViolations().each { PmdViolationsEvaluator.PmdViolation violation ->
            if (violation.isError()) {
                errors += 1
            } else {
                warnings += 1
            }
        }
        violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
    }

}
