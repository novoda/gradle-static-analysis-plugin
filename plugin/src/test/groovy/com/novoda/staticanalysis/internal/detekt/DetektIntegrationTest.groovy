package com.novoda.staticanalysis.internal.detekt

import com.novoda.test.DeployRulesTestRule
import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION
import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class DetektIntegrationTest {

    @ClassRule
    public static final DeployRulesTestRule DEPLOY_RULES = new DeployRulesTestRule(
            resourceDirs: [Fixtures.RULES_DIR],
            repoDir: new File(Fixtures.BUILD_DIR, 'rules'))

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forKotlinProject()]
    }

    @Rule
    public final TestProjectRule projectRule

    DetektIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldFailBuildWhenDetektWarningsOverTheThreshold() {
        def testProject = projectRule.newProject()
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')

        def detektConfiguration = """
        detekt { 
            profile('main') { 
                config = "${Fixtures.Detekt.RULES}" 
                output = "${testProject.projectDir()}/build/reports"
                input = "${Fixtures.Detekt.SOURCES_WITH_WARNINGS}"
            }
        }
        """

        testProject = testProject.withToolsConfig(detektConfiguration)

        TestProject.Result result = testProject
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
    }

    @Test
    void shouldFailBuildWhenDetektErrorsOverTheThreshold() {
        def testProject = projectRule.newProject()
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')

        def detektConfiguration = """
        detekt { 
            profile('main') { 
                config = "${Fixtures.Detekt.RULES}" 
                output = "${testProject.projectDir()}/build/reports"
                input = "${Fixtures.Detekt.SOURCES_WITH_ERRORS}"
            }
        }
        """

        testProject = testProject.withToolsConfig(detektConfiguration)

        TestProject.Result result = testProject
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
    }

}

