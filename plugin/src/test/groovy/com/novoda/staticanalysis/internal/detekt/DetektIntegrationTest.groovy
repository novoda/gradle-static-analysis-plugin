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
                // The input just needs to be configured for the tests. 
                // Probably detekt doesn't pick up the changed source sets. 
                // In a example project it was not needed.
                input = "${Fixtures.Detekt.SOURCES_WITH_WARNINGS}"
            }
        }
        """

        testProject = testProject.withToolsConfig(detektConfiguration)

        TestProject.Result result = testProject
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsDetektViolations(0, 1,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
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
        assertThat(result.logs).containsDetektViolations(1, 0,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailWhenDetektIsNotConfigured() throws Exception {
        def testProject = projectRule.newProject()
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')
                .build('check')

        TestProject.Result result = testProject

        assertThat(result.logs).doesNotContainDetektViolations()
    }

    @Test
    void shouldNotFailWhenWarningsAreWithinThreshold() throws Exception {
        def testProject = projectRule.newProject()
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 1
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
                .build('check')

        assertThat(result.logs).containsDetektViolations(0, 1,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailWhenErrorsAreWithinThreshold() throws Exception {
        def testProject = projectRule.newProject()
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 1
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
                .build('check')

        assertThat(result.logs).containsDetektViolations(1, 0,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailBuildWhenNoDetektWarningsOrErrorsEncounteredAndNoThresholdTrespassed() {
        def testProject = projectRule.newProject()
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')

        def detektConfiguration = """
        detekt { 
            profile('main') { 
                config = "${Fixtures.Detekt.RULES}" 
                output = "${testProject.projectDir()}/build/reports"
            }
        }
        """

        testProject = testProject.withToolsConfig(detektConfiguration)

        TestProject.Result result = testProject
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainDetektViolations()
    }
}

