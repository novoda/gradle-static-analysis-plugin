package com.novoda.staticanalysis.internal

import java.io.File

data class Violations(
        val name: String,
        var errors: Int = 0,
        var warnings: Int = 0,
        val reports: MutableList<File> = mutableListOf()
) {

    constructor(name: String): this(name, errors = 0, warnings = 0, reports = mutableListOf<File>())

    fun plus(errors: Int, warnings: Int, report: File) {
        this.errors = this.errors + errors
        this.warnings = this.warnings + warnings
        this.reports.add(report)
    }
}
