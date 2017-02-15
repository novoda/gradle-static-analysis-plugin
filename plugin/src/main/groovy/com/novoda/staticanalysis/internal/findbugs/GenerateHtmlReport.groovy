package com.novoda.staticanalysis.internal.findbugs

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec

class GenerateHtmlReport extends JavaExec {

    @Input
    File xmlReportFile

    @Input
    File htmlReportFile

    @Override
    void exec() {
        main = 'edu.umd.cs.findbugs.PrintingBugReporter'
        standardOutput = new FileOutputStream(htmlReportFile)
        args '-html', xmlReportFile
        super.exec()
    }
}
