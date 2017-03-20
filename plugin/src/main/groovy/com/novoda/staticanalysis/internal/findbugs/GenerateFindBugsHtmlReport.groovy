package com.novoda.staticanalysis.internal.findbugs

import org.gradle.api.tasks.JavaExec

class GenerateFindBugsHtmlReport extends JavaExec {

    File xmlReportFile
    File htmlReportFile

    @Override
    void exec() {
        if (xmlReportFile?.exists()) {
            main = 'edu.umd.cs.findbugs.PrintingBugReporter'
            standardOutput = new FileOutputStream(htmlReportFile)
            args '-html', xmlReportFile
            super.exec()
        }
    }
}
