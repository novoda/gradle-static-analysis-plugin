package com.novoda.staticanalysis.internal.findbugs

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
import static com.novoda.test.TestProjectSubject.assumeThat

@RunWith(Parameterized.class)
class FindbugsIntegrationTest {

    @Parameterized.Parameters
    public static List<Object[]> rules() {
        return [TestProjectRule.forJavaProject(), TestProjectRule.forAndroidProject()].collect { [it] as Object[] }
    }

    @Rule
    public final TestProjectRule projectRule

    FindbugsIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    public void shouldFailBuildWhenFindbugsWarningsOverTheThreshold() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('findbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldDetectMoreWarningsWhenEffortIsMaxAndReportLevelIsLow() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig("findbugs { effort = 'max' \n reportLevel = 'low'}")
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 2)
        assertThat(result.logs).containsFindbugsViolations(0, 3,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldFailBuildWhenFindbugsErrorsOverTheThreshold() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withToolsConfig('findbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsFindbugsViolations(1, 0,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldNotFailBuildWhenNoFindbugsWarningsOrErrorsEncounteredAndNoThresholdTrespassed() {
        TestProject.Result result = projectRule.newProject()
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withToolsConfig('findbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainFindbugsViolations()
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsWarningsAndErrorsEncounteredAndNoThresholdTrespassed() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(1, 2,
                result.buildFileUrl('reports/findbugs/debug.html'),
                result.buildFileUrl('reports/findbugs/release.html'))
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsConfiguredToNotIgnoreFailures() {
        projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs { ignoreFailures = false }')
                .build('check')
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsNotConfigured() {
        projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .build('check')
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsConfiguredToExcludePattern() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs { exclude "com/novoda/test/HighPriorityViolator.java" }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsConfiguredToIgnoreFaultySourceFolder() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('release', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig("findbugs { exclude project.fileTree('${SOURCES_WITH_HIGH_VIOLATION}') }" )
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    public void shouldNotFailBuildWhenFindbugsConfiguredToIgnoreFaultyJavaSourceSets() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isJavaProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs { exclude project.sourceSets.test.java.srcDirs }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldNotFailBuildWhenFindbugsConfiguredToIgnoreFaultyAndroidSourceSets() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('androidTest', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 1,
                result.buildFileUrl('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldCollectDuplicatedFindbugsWarningsAndErrorsAcrossAndroidVariantsForSharedSourceSets() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('main', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION, SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 10
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(2, 4,
                result.buildFileUrl('reports/findbugs/debug.html'),
                result.buildFileUrl('reports/findbugs/release.html'))
    }

    @Test
    public void shouldSkipFindbugsTasksForIgnoredFaultyJavaSourceSets() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isJavaProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs { exclude project.sourceSets.test.java.srcDirs }')
                .build('check')

        Truth.assertThat(result.outcome(':findbugsDebug')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':generateFindbugsDebugHtmlReport')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':findbugsTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':generateFindbugsTestHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    public void shouldSkipFindbugsTasksForIgnoredFaultyAndroidSourceSets() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('androidTest', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        Truth.assertThat(result.outcome(':findbugsDebugAndroidTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':generateFindbugsDebugAndroidTestHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
        Truth.assertThat(result.outcome(':findbugsDebug')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':generateFindbugsDebugHtmlReport')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':findbugsDebugUnitTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':generateFindbugsDebugUnitTestHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
        Truth.assertThat(result.outcome(':findbugsRelease')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':generateFindbugsReleaseHtmlReport')).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    public void shouldProvideNoClassesToFindbugsTaskWhenNoJavaSourcesToAnalyse() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isJavaProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 10
                }''')
                .withToolsConfig('findbugs { exclude project.sourceSets.test.java.srcDirs }')
                .withAdditionalConfiguration(addCheckFindbugsClassesTask())
                .build('checkFindbugsClasses')

        Truth.assertThat(result.outcome(':checkFindbugsClasses')).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    public void shouldProvideNoClassesToFindbugsTaskWhenNoAndroidSourcesToAnalyse() {
        TestProject project = projectRule.newProject()
        assumeThat(project).isAndroidProject()

        TestProject.Result result = project
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('androidTest', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .withAdditionalConfiguration(addCheckFindbugsClassesTask())
                .build('checkFindbugsClasses')

        Truth.assertThat(result.outcome(':checkFindbugsClasses')).isEqualTo(TaskOutcome.SUCCESS)
    }

    /**
     * The custom task created in the snippet below will check whether {@code Findbugs} tasks with
     * empty {@code source} will have empty {@code classes} too. </p>
     */
    private String addCheckFindbugsClassesTask() {
        '''
        project.task('checkFindbugsClasses') {
            dependsOn project.tasks.findByName('evaluateViolations')
            doLast {
                project.tasks.withType(FindBugs).all { findbugs ->
                    if (findbugs.source.isEmpty() && !findbugs.classes.isEmpty()) {
                        throw new GradleException("${findbugs.path}.source is empty but ${findbugs.path}.classes is not: \\n${findbugs.classes.files}")
                    }
                }
            }
        }'''
    }

}
