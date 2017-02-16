package com.novoda.staticanalysis

import org.gradle.api.Project

class LogsExtension {

    private ReportUrlRenderer reportUrlRenderer = ReportUrlRenderer.DEFAULT
    final Project project

    LogsExtension(Project project) {
        this.project = project
    }

    void baseReportUrl(String newBaseUrl, String localBaseUrl = "$project.buildDir/reports") {
        reportUrlRenderer { reportUrl ->
            newBaseUrl + reportUrl - ~/$localBaseUrl/
        }
    }

    void reportUrlRenderer(Closure<String> renderer) {
        reportUrlRenderer = new ReportUrlRenderer() {
            @Override
            String render(File report) {
                renderer.call(report.path)
            }
        }
    }

    ReportUrlRenderer getReportUrlRenderer() {
        reportUrlRenderer
    }

}
