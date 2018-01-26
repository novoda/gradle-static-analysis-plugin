package com.novoda.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

final class TestProjectRule<T extends TestProject> implements TestRule {

    private final Closure<T> projectFactory
    private final Closure<String> sourceSetNameFactory
    private final String label
    private T project

    static TestProjectRule<TestJavaProject> forJavaProject() {
        new TestProjectRule({ new TestJavaProject() }, { String name -> "project.sourceSets.$name" }, 'Java project')
    }

    static TestProjectRule<TestAndroidProject> forAndroidProject() {
        new TestProjectRule({ new TestAndroidProject() }, { String name -> "project.android.sourceSets.$name" }, 'Android project')
    }

    static TestProjectRule<TestAndroidKotlinProject> forAndroidKotlinProject() {
        new TestProjectRule({ new TestAndroidKotlinProject() }, { String name -> "project.android.sourceSets.$name" }, 'Android kotlin project')
    }

    static TestProjectRule<TestKotlinProject> forKotlinProject() {
        new TestProjectRule({ new TestKotlinProject() }, { String name -> "project.sourceSets.$name" }, 'Kotlin project')
    }

    private TestProjectRule(Closure projectFactory, Closure sourceSetNameFactory, String label) {
        this.projectFactory = projectFactory
        this.sourceSetNameFactory = sourceSetNameFactory
        this.label = label
    }

    public T newProject() {
        project = projectFactory.call()
        return project
    }

    public String printSourceSet(String name) {
        sourceSetNameFactory.call(name)
    }

    @Override
    Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                base.evaluate()
                project?.deleteDir()
            }
        }
    }

    @Override
    String toString() {
        label
    }

}
