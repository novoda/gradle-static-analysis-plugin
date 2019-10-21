package com.novoda.test

class TestAndroidProject extends TestProject<TestAndroidProject> {
    private static final Closure<String> TEMPLATE = { TestAndroidProject project ->
        """
buildscript {
    repositories { 
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.4'
        classpath 'com.novoda:gradle-static-analysis-plugin:local'
    }
}
plugins {
    ${formatPlugins(project)}
}
repositories { 
    google()
    jcenter()
}
apply plugin: 'com.android.library'
apply plugin: 'com.novoda.static-analysis'
android {
    compileSdkVersion 27

    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 27
        versionCode 1
        versionName '1.0'
    }                   
    lintOptions {
        disable 'OldTargetApi'
    }
    sourceSets {
        ${formatSourceSets(project)}
    }
    ${project.additionalAndroidConfig}
}
${formatExtension(project)}
"""
    }

    private final boolean shouldDisableAndroidLint

    private String additionalAndroidConfig = ''

    TestAndroidProject(boolean shouldDisableAndroidLint) {
        super(TEMPLATE)
        File localProperties = Fixtures.LOCAL_PROPERTIES
        if (localProperties.exists()) {
            withFile(localProperties, 'local.properties')
        }
        this.shouldDisableAndroidLint = shouldDisableAndroidLint
    }

    private static String formatSourceSets(TestProject project) {
        project.sourceSets
                .entrySet()
                .collect { Map.Entry<String, List<String>> entry ->
            """$entry.key {
            manifest.srcFile '${Fixtures.ANDROID_MANIFEST}'
            java {
                ${entry.value.collect { "srcDir '$it'" }.join('\n\t\t\t\t')}
            }
        }"""
        }
        .join('\n\t\t')
    }

    @Override
    List<String> defaultArguments() {
        defaultAndroidArguments() + super.defaultArguments()
    }

    private List<String> defaultAndroidArguments() {
        shouldDisableAndroidLint ? ['-x', 'lint'] : []
    }

    TestAndroidProject withAdditionalAndroidConfig(String additionalAndroidConfig) {
        this.additionalAndroidConfig = additionalAndroidConfig
        return this
    }
}
