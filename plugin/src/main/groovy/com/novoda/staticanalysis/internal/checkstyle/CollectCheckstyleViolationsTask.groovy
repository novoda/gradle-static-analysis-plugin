package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.internal.Violations
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CollectCheckstyleViolationsTask extends DefaultTask {

    File xmlReportFile

    Violations violations

    @TaskAction
    void run() {
        if (xmlReportFile?.exists()) {
            File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')
            GPathResult xml = new XmlSlurper().parse(xmlReportFile)
            int errors = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'error' }.size()
            int warnings = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'warning' }.size()
            violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
        }
    }

}
