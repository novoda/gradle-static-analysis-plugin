package com.novoda.staticanalysis.internal

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CollectViolationsTask extends DefaultTask {

    private File xmlReportFile
    private Violations violations

    void setXmlReportFile(File xmlReportFile) {
        this.xmlReportFile = xmlReportFile
    }

    void setViolations(Violations violations) {
        this.violations = violations
    }

    @TaskAction
    void run() {
        if (xmlReportFile?.exists()) {
            collectViolations(xmlReportFile, htmlReportFile, violations)
        }
    }

    File getHtmlReportFile() {
        new File(xmlReportFile.absolutePath - '.xml' + '.html')
    }

    abstract void collectViolations(File xmlReportFile, File htmlReportFile, Violations violations)
}
