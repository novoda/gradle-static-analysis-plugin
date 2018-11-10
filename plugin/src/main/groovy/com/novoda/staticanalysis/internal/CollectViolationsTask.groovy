package com.novoda.staticanalysis.internal

import com.novoda.staticanalysis.Violations
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

abstract class CollectViolationsTask extends DefaultTask {

    @InputFile
    @PathSensitive(RELATIVE)
    private File xmlReportFile
    private File htmlReportFile
    private Violations violations

    CollectViolationsTask() {
        onlyIf { xmlReportFile?.exists() }
    }

    void setXmlReportFile(File xmlReportFile) {
        this.xmlReportFile = xmlReportFile
    }

    void setHtmlReportFile(File htmlReportFile) {
        this.htmlReportFile = htmlReportFile
    }

    void setViolations(Violations violations) {
        this.violations = violations
    }

    @TaskAction
    final void run() {
        collectViolations(getXmlReportFile(), getHtmlReportFile(), violations)
    }

    File getXmlReportFile() {
        return xmlReportFile
    }

    File getHtmlReportFile() {
        htmlReportFile ?: new File(xmlReportFile.absolutePath - '.xml' + '.html')
    }

    abstract void collectViolations(File xmlReportFile, File htmlReportFile, Violations violations)
}
