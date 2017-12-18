package com.novoda.staticanalysis

import org.gradle.internal.logging.ConsoleRenderer
import java.io.File

interface ReportUrlRenderer {

    fun render(report: File): String

    object Default : ReportUrlRenderer {

        private val consoleRenderer = ConsoleRenderer()

        override fun render(report: File): String = consoleRenderer.asClickableFileUrl(report)
    }
}
