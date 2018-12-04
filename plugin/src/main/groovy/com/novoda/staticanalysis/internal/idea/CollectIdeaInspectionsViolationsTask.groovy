package com.novoda.staticanalysis.internal.idea

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CollectViolationsTask
import groovy.util.slurpersupport.GPathResult

class CollectIdeaInspectionsViolationsTask extends CollectViolationsTask {

    @Override
    void collectViolations(File xmlReportFile, File htmlReportFile, Violations violations) {
        GPathResult xml = new XmlSlurper().parse(xmlReportFile)
        int errors = xml.errors.problem.size()
        int warnings = xml.warnings.problem.size()
        violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
    }
}
