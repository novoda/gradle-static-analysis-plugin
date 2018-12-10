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
}

plugins {
    ${formatPlugins(project)}
    id 'com.novoda.static-analysis'
}

apply plugin: 'kotlin'

repositories { 
    jcenter()
}

${formatExtension(project)}
"""
    }

    TestKotlinProject() {
        super(TEMPLATE)
    }

}
