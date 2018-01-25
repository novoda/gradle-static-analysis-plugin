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

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class DetektIntegrationTest {

    @ClassRule
    public static final DeployRulesTestRule DEPLOY_RULES = new DeployRulesTestRule(
            resourceDirs: [Fixtures.RULES_DIR],
            repoDir: new File(Fixtures.BUILD_DIR, 'rules'))

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forKotlinProject(), TestProjectRule.forAndroidKotlinProject()]
    }

    @Rule
    public final TestProjectRule projectRule

    DetektIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldFailBuildWhenDetektWarningsOverTheThreshold() {
        def result = createProjectWithZeroThreshold(Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsDetektViolations(0, 1,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldFailBuildWhenDetektErrorsOverTheThreshold() {
        def result = createProjectWithZeroThreshold(Fixtures.Detekt.SOURCES_WITH_ERRORS)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsDetektViolations(1, 0,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailWhenDetektIsNotConfigured() throws Exception {
        def result = createProjectWithoutDetekt()
                .build('check')

        assertThat(result.logs).doesNotContainDetektViolations()
    }

    @Test
    void shouldNotFailWhenWarningsAreWithinThreshold() throws Exception {
        def result = createProjectWith(Fixtures.Detekt.SOURCES_WITH_WARNINGS, 1, 0)
                .build('check')

        assertThat(result.logs).containsDetektViolations(0, 1,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailWhenErrorsAreWithinThreshold() throws Exception {
        def result = createProjectWith(Fixtures.Detekt.SOURCES_WITH_ERRORS, 0, 1)
                .build('check')

        assertThat(result.logs).containsDetektViolations(1, 0,
                result.buildFileUrl('reports/detekt-checkstyle.html'))
    }

    @Test
    void shouldNotFailBuildWhenNoDetektWarningsOrErrorsEncounteredAndNoThresholdTrespassed() {
        def testProject = projectRule.newProject()
                .withPlugin("io.gitlab.arturbosch.detekt", "1.0.0.RC6-2")
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')
        testProject = testProject.withToolsConfig(detektConfigurationWithoutInput(testProject))

        TestProject.Result result = testProject
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainDetektViolations()
    }

    @Test
    void shouldFailBuildWhenDetektConfiguredButNotApplied() {
        def testProject = projectRule.newProject()
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')

        testProject = testProject.withToolsConfig(detektConfiguration(testProject, Fixtures.Detekt.SOURCES_WITH_ERRORS))

        TestProject.Result result = testProject
                .buildAndFail('check')

        assertThat(result.logs).containsDetektNotApplied()
    }

    private TestProject createProjectWithZeroThreshold(File sources) {
        createProjectWith(sources)
    }

    private TestProject createProjectWith(File sources, int maxWarnings = 0, int maxErrors = 0) {
        def testProject = projectRule.newProject()
                .withPlugin("io.gitlab.arturbosch.detekt", "1.0.0.RC6-2")
                .withSourceSet('main', sources)
                .withPenalty("""{
                    maxWarnings = ${maxWarnings}
                    maxErrors = ${maxErrors}
                }""")

        testProject.withToolsConfig(detektConfiguration(testProject, sources))
    }

    private TestProject createProjectWithoutDetekt() {
        projectRule.newProject()
                .withPlugin("io.gitlab.arturbosch.detekt", "1.0.0.RC6-2")
                .withSourceSet('main', Fixtures.Detekt.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')
    }

    private static GString detektConfiguration(TestProject testProject, File input) {
        """
        detekt { 
            profile('main') { 
                config = "${Fixtures.Detekt.RULES}" 
                output = "${testProject.projectDir()}/build/reports"
                // The input just needs to be configured for the tests. 
                // Probably detekt doesn't pick up the changed source sets. 
                // In a example project it was not needed.
                input = "${input}"
            }
        }
        """
    }

    private static GString detektConfigurationWithoutInput(TestProject testProject) {
        """
        detekt { 
            profile('main') { 
                config = "${Fixtures.Detekt.RULES}" 
                output = "${testProject.projectDir()}/build/reports"
            }
        }
        """
    }
}
