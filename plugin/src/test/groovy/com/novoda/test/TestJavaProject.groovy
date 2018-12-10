package com.novoda.test

final class TestJavaProject extends TestProject<TestJavaProject> {

    private static final Closure<String> TEMPLATE = { TestProject project ->
        """
plugins {
    ${formatPlugins(project)} 
    id 'com.novoda.static-analysis'
}
repositories {
    jcenter()
}
apply plugin: 'java'
${formatExtension(project)}
"""
    }

    TestJavaProject() {
        super(TEMPLATE)
    }
}
