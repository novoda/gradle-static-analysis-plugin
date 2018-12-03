package com.novoda.test

final class TestKotlinProject extends TestProject<TestKotlinProject> {

    private static final Closure<String> TEMPLATE = { TestProject project ->
        """
buildscript {
    repositories { 
        jcenter()
    }
    dependencies {
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.10'
    }
    ${project.additionalBuildscriptConfiguration}
}

plugins {
    ${formatPlugins(project)}
    id 'com.novoda.static-analysis'
}

apply plugin: 'kotlin'

repositories { 
    jcenter()
}

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
