package com.novoda.staticanalysis

import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

class StaticAnalysisPluginTest {

    @Rule
    public final TestProjectRule rule = new TestProjectRule({ new EmptyProject() }, { "" }, 'Empty project')

    @Test
    void shouldNotFailWhenNoJavaOrAndroidPluginsAreApplied() {
        rule.newProject()
                .build("help")
    }

    private static class EmptyProject extends TestProject<EmptyProject> {
        private static final Closure<String> TEMPLATE = { TestProject project ->
            """
buildscript {
    dependencies {
        classpath 'com.novoda:gradle-static-analysis-plugin:local'
    }
}
apply plugin: 'com.novoda.static-analysis'
"""
        }

        EmptyProject() {
            super(TEMPLATE)
        }
    }
}
