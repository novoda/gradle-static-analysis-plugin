package com.novoda.staticanalysis.internal.idea

import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class IdeaIntegrationTest {

    @Parameterized.Parameters
    static def rules() {
        return [
                [TestProjectRule.forKotlinProject(), '0.2.2'],
                [TestProjectRule.forAndroidKotlinProject(), '0.2.2'],
                [TestProjectRule.forJavaProject(), '0.2.2'],
        ]*.toArray()
    }

    @Rule
    public final TestProjectRule projectRule
    private final toolsVersion

    IdeaIntegrationTest(TestProjectRule projectRule, toolsVersion) {
        this.projectRule = projectRule
        this.toolsVersion = toolsVersion
    }

    @Test
    void shouldNotFailWhenIdeaIsNotConfigured() {
        def result = createProject(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainIdeaViolations()
    }

    private TestProject createProject(File sources) {
        projectRule.newProject()
                .withAdditionalBuildscriptConfiguration("""
                    repositories {
                        maven { url 'https://dl.bintray.com/kotlin/kotlin-dev' }
                    }
                    dependencies {
                        classpath 'org.jetbrains.intellij.plugins:inspection-plugin:$toolsVersion'
                    }
                """)
                .withAdditionalConfiguration("apply plugin: 'org.jetbrains.intellij.inspections'")
                .withSourceSet('main', sources)
                .withPenalty('failFast')
    }
}
