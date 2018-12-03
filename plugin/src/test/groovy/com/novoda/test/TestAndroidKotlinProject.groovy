package com.novoda.test

class TestAndroidKotlinProject extends TestProject<TestAndroidKotlinProject> {
    private static final Closure<String> TEMPLATE = { TestAndroidKotlinProject project ->
        """
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.4'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.20'
    }
    ${project.additionalBuildscriptConfiguration}
}
plugins {
    ${formatPlugins(project)}   
    id 'com.novoda.static-analysis'
}
repositories {
    google()
    jcenter()
}
apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'

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

    private String additionalAndroidConfig = ''

    TestAndroidKotlinProject() {
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

    @Override
    List<String> defaultArguments() {
        ['-x', 'lint'] + super.defaultArguments()
    }

    TestAndroidKotlinProject withAdditionalAndroidConfig(String additionalAndroidConfig) {
        this.additionalAndroidConfig = additionalAndroidConfig
        return this
    }
}
