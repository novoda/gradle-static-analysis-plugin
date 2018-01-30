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
        classpath 'com.android.tools.build:gradle:3.0.1'
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
android {
    compileSdkVersion 27
    buildToolsVersion '27.0.0'

    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 27
        versionCode 1
        versionName '1.0'
    }
    sourceSets {
        ${formatSourceSets(project)}
    }
    ${project.additionalAndroidConfig}
}
${formatExtension(project)}
"""
    }

    private String additionalAndroidConfig = ''

    TestAndroidProject() {
        super(TEMPLATE)
        File localProperties = Fixtures.LOCAL_PROPERTIES
        if (localProperties.exists()) {
            withFile(localProperties, 'local.properties')
        }
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

    TestAndroidProject withAdditionalAndroidConfig(String additionalAndroidConfig) {
        this.additionalAndroidConfig = additionalAndroidConfig
        return this
    }
}
