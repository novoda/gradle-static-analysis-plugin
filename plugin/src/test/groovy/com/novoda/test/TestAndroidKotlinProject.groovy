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
    sourceSets.main {
        manifest.srcFile '${Fixtures.ANDROID_MANIFEST}'
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

    @Override
    List<String> defaultArguments() {
        ['-x', 'lint'] + super.defaultArguments()
    }

    TestAndroidKotlinProject withAdditionalAndroidConfig(String additionalAndroidConfig) {
        this.additionalAndroidConfig = additionalAndroidConfig
        return this
    }
}
