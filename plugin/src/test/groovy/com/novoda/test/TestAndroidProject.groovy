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

    TestAndroidProject() {
        super(TEMPLATE)
        File localProperties = Fixtures.LOCAL_PROPERTIES
        if (localProperties.exists()) {
            withFile(localProperties, 'local.properties')
        }
    }

    TestAndroidProject withAdditionalAndroidConfig(String additionalAndroidConfig) {
        this.additionalAndroidConfig = additionalAndroidConfig
        return this
    }
}
