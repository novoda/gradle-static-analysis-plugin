package com.novoda.staticanalysis.internal.lint

import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class LintIntegrationTest {

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forAndroidProject()]
    }

    @Rule
    public final TestProjectRule projectRule

    LintIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldFailBuildWhenLintWarningsOverTheThreshold() throws Exception {
        def result = createProjectWith(Fixtures.Lint.SOURCES_WITH_WARNINGS, 0, 0)
                .buildAndFail('check')

        assertThat(result.logs).containsLintViolations(0, 1)
    }

    private TestProject createProjectWith(File sources, int maxWarnings = 0, int maxErrors = 0) {
        def testProject = projectRule.newProject()
                .withSourceSet('main', sources)
                .withPenalty("""{
                    maxWarnings = ${maxWarnings}
                    maxErrors = ${maxErrors}
                }""")

        testProject.withToolsConfig(lintConfiguration(testProject, sources))
    }

    private static GString lintConfiguration(TestProject testProject, File input) {
        """
        lintOptions { 
            //lintConfig = file("${Fixtures.Detekt.RULES}") 
            xmlOutput = "${testProject.projectDir()}/build/reports"
        }
        """
    }
}
