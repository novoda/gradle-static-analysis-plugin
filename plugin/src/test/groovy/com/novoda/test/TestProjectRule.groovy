package com.novoda.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

final class TestProjectRule implements TestRule {

    private final Closure<TestProject> projectFactory
    private final Closure<String> sourceSetNameFactory
    private TestProject project

    static TestProjectRule forJavaProject() {
        new TestProjectRule({ new TestJavaProject() }, { String name -> "project.sourceSets.$name" })
    }

    static TestProjectRule forAndroidProject() {
        new TestProjectRule({ new TestAndroidProject() }, { String name -> "project.android.sourceSets.$name" })
    }

    private TestProjectRule(Closure projectFactory, Closure sourceSetNameFactory) {
        this.projectFactory = projectFactory
        this.sourceSetNameFactory = sourceSetNameFactory
    }

    public TestProject newProject() {
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

}
