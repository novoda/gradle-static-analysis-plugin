package com.novoda.test

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DeployRulesTestRule implements TestRule {

    List<File> resourceDirs
    File repoDir
    String groupId = 'test'
    String artifactId = 'rules'
    String version = 'unreleased'

    String getMavenCoordinates() {
        "$groupId:$artifactId:$version"
    }

    @Override
    Statement apply(Statement base, Description description) {
        new Statement() {
            @Override
            void evaluate() throws Throwable {
                cleanRepo()
                File projectDir = createProjectDir("${System.currentTimeMillis()}")
                createBuildScript(projectDir)
                createGradleSettings(projectDir)
                GradleRunner.create()
                        .withProjectDir(projectDir)
                        .withDebug(true)
                        .withTestKitDir(new File(projectDir, "test-kit"))
                        .forwardStdOutput(new OutputStreamWriter(System.out))
                        .forwardStdError(new OutputStreamWriter(System.out))
                        .withArguments('clean', 'publish')
                        .build()
                base.evaluate()
                projectDir.deleteDir()
            }

        }
    }

    private void cleanRepo() {
        def artifactDir = new File(repoDir, "${groupId.replace('.', '/')}/$artifactId/$version")
        if (artifactDir.exists()) {
            artifactDir.deleteDir()
        }
    }

    private void createGradleSettings(File projectDir) {
        new File(projectDir, 'settings.gradle').text = ""
    }

    private void createBuildScript(File projectDir) {
        new File(projectDir, 'build.gradle').text = """
buildscript {
    repositories {
        jcenter()
    }
}

version='$version'

apply plugin: 'java'

sourceSets {
    main {
        resources {
            srcDirs = ${resourceDirs.collect { "'$it.path'" }}
        }
    }
}

apply plugin: 'maven-publish'

publishing {
    repositories {
        maven { url '${repoDir}' }
    }
    publications {
        mavenJava(MavenPublication) {
            groupId '$groupId'
            artifactId '$artifactId'
            from components.java
        }
    }
}"""
    }

    private static File createProjectDir(String path) {
        File dir = new File(Fixtures.BUILD_DIR, "test-projects/$path")
        dir.deleteDir()
        dir.mkdirs()
        return dir
    }

}
