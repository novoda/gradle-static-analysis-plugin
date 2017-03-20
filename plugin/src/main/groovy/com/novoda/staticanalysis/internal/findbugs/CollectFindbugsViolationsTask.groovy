package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.internal.CollectViolationsTask
import com.novoda.staticanalysis.internal.Violations

class CollectFindbugsViolationsTask extends CollectViolationsTask {

    @Override
    void collectViolations(File xmlReportFile, File htmlReportFile, Violations violations) {
        def evaluator = new FinbugsViolationsEvaluator(xmlReportFile)
        violations.addViolations(evaluator.errorsCount(), evaluator.warningsCount(), htmlReportFile)
    }
}
