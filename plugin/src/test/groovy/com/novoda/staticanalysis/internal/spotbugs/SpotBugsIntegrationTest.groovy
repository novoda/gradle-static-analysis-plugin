package com.novoda.staticanalysis.internal.spotbugs

import com.google.common.truth.Truth
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.Fixtures.Findbugs.*
import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class SpotBugsIntegrationTest {

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forJavaProject(), TestProjectRule.forAndroidProject()]
    }

    @Rule
    public final TestProjectRule projectRule

    SpotBugsIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldFailBuildWhenSpotBugsWarningsOverTheThreshold() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('spotbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    void shouldFailBuildAfterSecondRunWhenSpotBugsWarningsStillOverTheThreshold() {
        def project = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('spotbugs {}')

        TestProject.Result result = project.buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))

        result = project.buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    void shouldDetectMoreWarningsWhenEffortIsMaxAndReportLevelIsLow() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig("spotbugs { effort = 'max' \n reportLevel = 'low'}")
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 2)
        assertThat(result.logs).containsSpotBugsViolations(0, 3,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    void shouldFailBuildWhenSpotBugsErrorsOverTheThreshold() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withToolsConfig('spotbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsSpotBugsViolations(1, 0,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    void shouldNotFailBuildWhenNoSpotBugsWarningsOrErrorsEncounteredAndNoThresholdTrespassed() {
        TestProject.Result result = createProjectWith()
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withToolsConfig('spotbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainSpotBugsViolations()
    }

    @Test
    void shouldNotFailBuildWhenSpotBugsWarningsAndErrorsEncounteredAndNoThresholdTrespassed() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('spotbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsSpotBugsViolations(1, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'),
                result.buildFileUrl('reports/spotbugs/release.html'))
    }

    @Test
    void shouldNotFailBuildWhenSpotBugsConfiguredToNotIgnoreFailures() {
        createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('spotbugs { ignoreFailures = false }')
                .build('check')
    }

    @Test
    void shouldBeUpToDateWhenCheckTaskRunsAgain() {
        def project = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('spotbugs {}')

        project.build('check')

        def result = project.build('check')

        Truth.assertThat(result.outcome(':spotbugsDebug')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':generateSpotbugsDebugHtmlReport')).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    void shouldNotGenerateHtmlWhenDisabled() {
        def result = createProjectWith()
                .withSourceSet('main', SOURCES_WITH_LOW_VIOLATION)
                .withToolsConfig('''spotbugs { 
                    htmlReportEnabled false 
                }''')
                .build('check')

        Truth.assertThat(result.tasksPaths).doesNotContain(':generateSpotBugsDebugHtmlReport')
    }

    private TestProject createProjectWith() {
        projectRule.newProject()
                .withPlugin('com.github.spotbugs', "2.0.0")
    }
}
