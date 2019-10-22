package com.novoda.staticanalysis.internal.spotbugs

import com.google.common.truth.Truth
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.Fixtures.Findbugs.*
import static com.novoda.test.LogsSubject.assertThat
import static com.novoda.test.TestProjectSubject.assumeThat

@RunWith(Parameterized.class)
class SpotBugsIntegrationTest {

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forJavaProject()/*, TestProjectRule.forAndroidProject()*/]
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
    void shouldNotFailBuildWhenSpotBugsNotConfigured() {
        createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .build('check')
    }

    @Test
    @Ignore
    void shouldNotFailBuildWhenSpotBugsConfiguredToExcludePattern() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('spotbugs { exclude "com/novoda/test/HighPriorityViolator.java" }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    @Ignore
    void shouldNotFailBuildWhenSpotBugsConfiguredToExcludeFaultySourceFolder() {
        TestProject.Result result = createProjectWith()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig("spotbugs { exclude project.fileTree('${SOURCES_WITH_HIGH_VIOLATION}') }")
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    @Ignore
    void shouldNotFailBuildWhenSpotBugsConfiguredToIgnoreFaultyJavaSourceSets() {
        TestProject project = createProjectWith()
        assumeThat(project).isJavaProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('spotbugs { exclude project.sourceSets.test.java.srcDirs }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsSpotBugsViolations(0, 2,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    @Ignore
    void shouldNotFailBuildWhenSpotBugsConfiguredToIgnoreFaultyAndroidSourceSets() {
        TestProject project = createProjectWith()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('androidTest', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('''spotbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsSpotBugsViolations(0, 1,
                result.buildFileUrl('reports/spotbugs/debug.html'))
    }

    @Test
    @Ignore
    void shouldSkipSpotBugsTasksForIgnoredFaultyAndroidSourceSets() {
        TestProject project = createProjectWith()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('androidTest', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('''spotbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        Truth.assertThat(result.outcome(':spotbugsDebugAndroidTest')).isEqualTo(TaskOutcome.NO_SOURCE)
        Truth.assertThat(result.outcome(':generateSpotBugsDebugAndroidTestHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
        Truth.assertThat(result.outcome(':spotbugsDebug')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':generateSpotBugsDebugHtmlReport')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':spotbugsDebugUnitTest')).isEqualTo(TaskOutcome.NO_SOURCE)
        Truth.assertThat(result.outcome(':generateSpotBugsDebugUnitTestHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
        Truth.assertThat(result.outcome(':spotbugsRelease')).isEqualTo(TaskOutcome.NO_SOURCE)
        Truth.assertThat(result.outcome(':generateSpotBugsReleaseHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    void shouldNotFailBuildWhenSpotBugsIsConfiguredMultipleTimes() {
        createProjectWith()
                .withSourceSet('main', SOURCES_WITH_LOW_VIOLATION)
                .withPenalty('none')
                .withToolsConfig("""
                    spotbugs { }
                    spotbugs {
                        ignoreFailures = false
                    }
                """)
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
        Truth.assertThat(result.outcome(':generateSpotBugsDebugHtmlReport')).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    @Ignore
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
