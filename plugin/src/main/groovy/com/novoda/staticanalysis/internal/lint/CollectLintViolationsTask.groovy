package com.novoda.staticanalysis.internal.lint

import com.novoda.staticanalysis.internal.CollectViolationsTask
import com.novoda.staticanalysis.internal.Violations
import groovy.util.slurpersupport.GPathResult

class CollectLintViolationsTask extends CollectViolationsTask {

    @Override
    void collectViolations(File xmlReportFile, File htmlReportFile, Violations violations) {
        GPathResult xml = new XmlSlurper().parse(xmlReportFile)
        int errors = xml.'**'.findAll { node -> node.name() == 'issue' && node.@severity == 'Error' }.size()
        int warnings = xml.'**'.findAll { node -> node.name() == 'issue' && node.@severity == 'Warning' }.size()
        violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
    }

}
