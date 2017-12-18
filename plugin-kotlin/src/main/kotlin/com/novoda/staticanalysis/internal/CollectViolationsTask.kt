package com.novoda.staticanalysis.internal

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CollectViolationsTask : DefaultTask() {

    var xmlReportFile: File? = null
    var violations: Violations? = null

    init {
        onlyIf { xmlReportFile?.exists() ?: false }
    }

    @TaskAction
    fun run() {
        collectViolations(xmlReportFile, htmlReportFile, violations)
    }

    private val htmlReportFile: File?
        get() = xmlReportFile?.swapExtension(fromExtension = ".xml", toExtension = ".html")

    private fun File.swapExtension(fromExtension: String, toExtension: String) =
        File(absolutePath.removeSuffix(fromExtension) + toExtension)

    abstract fun collectViolations(xmlReportFile: File?, htmlReportFile: File?, violations: Violations?)
}
