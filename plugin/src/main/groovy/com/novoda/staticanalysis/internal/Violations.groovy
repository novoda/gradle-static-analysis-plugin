package com.novoda.staticanalysis.internal;

class Violations {
    private final String toolName
    private int errors = 0
    private int warnings = 0
    private List<String> reports = []

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

    List<String> getReports() {
        return reports
    }

    public void addViolations(int errors, int warnings, String reportUrl) {
        this.errors += errors
        this.warnings += warnings
        if (errors > 0 || warnings > 0) {
            reports += reportUrl
        }
    }

}
