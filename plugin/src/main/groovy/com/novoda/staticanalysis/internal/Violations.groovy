package com.novoda.staticanalysis.internal;

class Violations {
    private final String toolName
    private int errors = 0
    private int warnings = 0
    private List<File> reports = []

    Violations(String toolName) {
        this.toolName = toolName
    }

    String getName() {
        return toolName
    }

    int getErrors() {
        errors
    }

    int getWarnings() {
        warnings
    }

    List<File> getReports() {
        return reports
    }

    void addViolations(int errors, int warnings, File report) {
        this.errors += errors
        this.warnings += warnings
        if (errors > 0 || warnings > 0) {
            reports += report
        }
    }

    boolean isEmpty() {
        errors == 0 && warnings == 0
    }
}
