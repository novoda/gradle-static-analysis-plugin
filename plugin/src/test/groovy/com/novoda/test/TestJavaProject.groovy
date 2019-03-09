package com.novoda.test

final class TestJavaProject extends TestProject<TestJavaProject> {

    private static final Closure<String> TEMPLATE = { TestProject project ->
        """
buildscript {
    dependencies {
        classpath 'com.novoda:gradle-static-analysis-plugin:local'
    }
}
plugins {
    ${formatPlugins(project)} 
}
repositories {
    jcenter()
}
apply plugin: 'java'
apply plugin: 'com.novoda.static-analysis'

sourceSets {
    ${formatSourceSets(project)}
}
${formatExtension(project)}
"""
    }

    TestJavaProject() {
        super(TEMPLATE)
    }

    private static String formatSourceSets(TestProject project) {
        project.sourceSets
                .entrySet()
                .collect { Map.Entry<String, List<String>> entry ->
            """$entry.key {
        java {
            ${entry.value.collect { "srcDir '$it'" }.join('\n\t\t\t\t')}
        }
    }"""
        }
        .join('\n\t')
    }
}
