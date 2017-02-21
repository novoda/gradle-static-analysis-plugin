package com.novoda.staticanalysis

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.resources.TextResource

class RulesExtension {
    final String name
    final Project project
    Configuration configuration

    RulesExtension(String name, Project project) {
        this.name = name
        this.project = project
    }

    void maven(String coordinates) {
        def configurationName = "staticAnalysis${name.capitalize()}"
        configuration = project.configurations.create(configurationName)
        project.dependencies.add(configurationName, coordinates)
    }

    TextResource getAt(String relativePath) {
        project.resources.text.fromArchiveEntry(configuration, relativePath)
    }
}
