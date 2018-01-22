package com.novoda.staticanalysis.internal.detekt

import com.novoda.staticanalysis.StaticAnalysisExtension
import org.gradle.api.Project

public class DetektConfigurator {

    protected final Project project

    protected DetektConfigurator(Project project) {
        this.project = project
    }

    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext."detekt" = { Closure config ->

            if (!isKotlinProject(project)) {
                return
            }

            project.apply plugin: 'io.gitlab.arturbosch.detekt'

            if (project.tasks.findByName('detektCheck')) {
                def detektTask = project.tasks.findByName('detektCheck')
                project.tasks.findByName('check').dependsOn(detektTask)

            }
        }
    }

    private static boolean isKotlinProject(final Project project) {
        final boolean isKotlin = project.plugins.hasPlugin('kotlin')
        final boolean isKotlinAndroid = project.plugins.hasPlugin('kotlin-android')
        final boolean isKotlinPlatformCommon = project.plugins.hasPlugin('kotlin-platform-common')
        final boolean isKotlinPlatformJvm = project.plugins.hasPlugin('kotlin-platform-jvm')
        final boolean isKotlinPlatformJs = project.plugins.hasPlugin('kotlin-platform-js')
        return isKotlin || isKotlinAndroid || isKotlinPlatformCommon || isKotlinPlatformJvm || isKotlinPlatformJs
    }
}
