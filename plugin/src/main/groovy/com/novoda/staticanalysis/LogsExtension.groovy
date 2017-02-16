package com.novoda.staticanalysis

import org.gradle.api.Project

/**
 * This extension can be used to change the rendering of the logs printed by the plugin.
 */
class LogsExtension {

    private ReportUrlRenderer reportUrlRenderer = ReportUrlRenderer.DEFAULT
    final Project project

    LogsExtension(Project project) {
        this.project = project
    }

    /**
     * This method will create a new {@code ReportUrlRenderer} using the transformation closure provided.
     * the closure will receive the report as {@code File} and should provide a {@code String} as output.
     *
     * Here an example os custom renderer that will render report urls as markdown links (?!):
     * <pre>
     *     staticAnalysis {
     *         ...
     *         logs {
     *             reportUrlRenderer { report -> "$[report.name]($report.path)" }
     *         }
     *     }
     * </pre>
     *
     * @param renderer a closure that will transform a given {@code File} into a {@code String}
     */
    void reportUrlRenderer(Closure<String> renderer) {
        reportUrlRenderer = new ReportUrlRenderer() {
            @Override
            String render(File report) {
                renderer.call(report)
            }
        }
    }

    /**
     * This method provides a convenient way for creating a custom {@code ReportUrlRenderer} that will replace the base
     * url of each report in the logs with one of choice. The target for the replacement by default is set to the
     * reports dir of the project, but can be changes if needed.
     *
     * @param newBaseUrl the base url to be used
     * @param localBaseUrl optional, the local base url to be replaced with {@code newBaseUrl}
     */
    void baseReportUrl(String newBaseUrl, String localBaseUrl = "$project.buildDir/reports") {
        reportUrlRenderer { report -> newBaseUrl + (report.path - ~/$localBaseUrl/) }
    }

    ReportUrlRenderer getReportUrlRenderer() {
        reportUrlRenderer
    }

}
