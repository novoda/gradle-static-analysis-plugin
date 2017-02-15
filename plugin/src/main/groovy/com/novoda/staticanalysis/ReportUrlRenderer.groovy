package com.novoda.staticanalysis

import org.gradle.internal.logging.ConsoleRenderer

interface ReportUrlRenderer {

    String render(File report)

    ReportUrlRenderer DEFAULT = new Default()

    private static class Default implements ReportUrlRenderer {
        private final ConsoleRenderer consoleRenderer = new ConsoleRenderer()
        @Override
        String render(File report) {
            consoleRenderer.asClickableFileUrl(report)
        }
    }

}
