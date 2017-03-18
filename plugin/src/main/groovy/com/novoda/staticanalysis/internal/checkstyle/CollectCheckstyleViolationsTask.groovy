package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.internal.CollectViolationsTask
import com.novoda.staticanalysis.internal.Violations
import groovy.util.slurpersupport.GPathResult

class CollectCheckstyleViolationsTask extends CollectViolationsTask {

    @Override
    void collectViolations(File xmlReportFile, Violations violations) {
        File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')
        GPathResult xml = new XmlSlurper().parse(xmlReportFile)
        int errors = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'error' }.size()
        int warnings = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'warning' }.size()
        violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
    }

}
