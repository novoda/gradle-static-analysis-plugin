package com.novoda.test

final class TestKotlinProject extends TestProject<TestKotlinProject> {

    private static final Closure<String> TEMPLATE = { TestProject project ->
        """
buildscript {
    repositories { 
        jcenter()
         maven {
            url "https://plugins.gradle.org/m2/"
         }
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.10'
        classpath 'gradle.plugin.io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.0.0.RC6-2'
    }
}

plugins {
    ${formatPlugins(project)}
}

apply plugin: 'kotlin'
sourceSets {
    ${formatSourceSets(project)}
}
${formatExtension(project)}
"""
    }

    TestKotlinProject() {
        super(TEMPLATE)
    }

    private static String formatSourceSets(TestProject project) {
        project.sourceSets
                .entrySet()
                .collect { Map.Entry<String, List<String>> entry ->
            """$entry.key {
        kotlin {
            ${entry.value.collect { "srcDir '$it'" }.join('\n\t\t\t\t')}
        }
    }"""
        }
        .join('\n\t')
    }
}
