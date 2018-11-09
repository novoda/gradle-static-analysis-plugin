package com.novoda.staticanalysis.internal.findbugs

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

class GenerateFindBugsHtmlReport extends JavaExec {

    @InputFile
    @PathSensitive(RELATIVE)
    File xmlReportFile
    @OutputFile
    @PathSensitive(RELATIVE)
    File htmlReportFile

    GenerateFindBugsHtmlReport() {
        onlyIf { xmlReportFile?.exists() }
    }

    @Override
    void exec() {
        main = 'edu.umd.cs.findbugs.PrintingBugReporter'
        standardOutput = new FileOutputStream(htmlReportFile)
        args '-html', xmlReportFile
        super.exec()
    }
}
