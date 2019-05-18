package com.novoda.staticanalysis.internal

import org.gradle.api.GradleException

class Exceptions {

    /**
     * In case the cause is a GradleException rethrow because it probably already has useful information.
     * If not, wrap it and put useful information about the state of the integrations and versions.
     */
    static void handleException(String message, Exception cause) {
        if (cause instanceof GradleException) {
            throw cause
        }
        throw new GradleException(message, cause)
    }
}
