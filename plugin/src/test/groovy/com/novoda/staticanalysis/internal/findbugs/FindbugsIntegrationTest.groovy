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
                .withFindbugs('findbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFile('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldDetectMoreWarningsWhenEffortIsMaxAndReportLevelIsLow() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_LOW_VIOLATION, SOURCES_WITH_MEDIUM_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withFindbugs("findbugs { effort = 'max' \n reportLevel = 'low'}")
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 2)
        assertThat(result.logs).containsFindbugsViolations(0, 3,
                result.buildFile('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldFailBuildWhenFindbugsErrorsOverTheThreshold() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('debug', SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withFindbugs('findbugs {}')
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsFindbugsViolations(1, 0,
                result.buildFile('reports/findbugs/debug.html'))
    }

    @Test
    public void shouldNotFailBuildWhenNoFindbugsWarningsOrErrorsEncounteredAndNoThresholdTrespassed() {
        TestProject.Result result = projectRule.newProject()
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withFindbugs('findbugs {}')
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
                .withFindbugs('findbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(1, 2,
                result.buildFile('reports/findbugs/debug.html'),
                result.buildFile('reports/findbugs/release.html'))
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
                .withFindbugs('findbugs { ignoreFailures = false }')
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
                .withFindbugs('findbugs { exclude "com/novoda/test/HighPriorityViolator.java" }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFile('reports/findbugs/debug.html'))
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
                .withFindbugs("findbugs { exclude project.fileTree('${SOURCES_WITH_HIGH_VIOLATION}') }")
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFile('reports/findbugs/debug.html'))
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
                .withFindbugs('findbugs { exclude project.sourceSets.test.java.srcDirs }')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFile('reports/findbugs/debug.html'))
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
                .withFindbugs('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 1,
                result.buildFile('reports/findbugs/debug.html'))
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
                .withFindbugs('findbugs {}')
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(2, 4,
                result.buildFile('reports/findbugs/debug.html'),
                result.buildFile('reports/findbugs/release.html'))
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
                .withFindbugs('findbugs { exclude project.sourceSets.test.java.srcDirs }')
                .build('check')

        Truth.assertThat(result.outcome(':findbugsDebug')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':findbugsTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
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
                .withFindbugs('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .build('check')

        Truth.assertThat(result.outcome(':findbugsDebugAndroidTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':findbugsDebug')).isEqualTo(TaskOutcome.SUCCESS)
        Truth.assertThat(result.outcome(':findbugsDebugUnitTest')).isEqualTo(TaskOutcome.UP_TO_DATE)
        Truth.assertThat(result.outcome(':findbugsRelease')).isEqualTo(TaskOutcome.UP_TO_DATE)
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
                .withFindbugs('findbugs { exclude project.sourceSets.test.java.srcDirs }')
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
                .withFindbugs('''findbugs {
                    exclude project.android.sourceSets.test.java.srcDirs
                    exclude project.android.sourceSets.androidTest.java.srcDirs
                }''')
                .withAdditionalConfiguration(addCheckFindbugsClassesTask())
                .build('checkFindbugsClasses')

        Truth.assertThat(result.outcome(':checkFindbugsClasses')).isEqualTo(TaskOutcome.SUCCESS)
    }

    /**
     * <p>While integrating the plugin in one of our projects we realised that Findbugs was analysing a source folder
     * even though an {@code exclude} rule was provided for it.
     * After quite some investigation we realised that the issue is related to the way we create {@code include} patterns
     * from the filtered collection in {@code source}: given a {@code Findbugs} task we peek into the filtered set
     * of source files and for each of them we create an {@code include} rule for the related class(es).
     * Let's assume {@code source} contains {@code foo/Bar.java}; then the plugin will produce
     * {@code foo/Bar*} as include pattern to allow the {@code Bar.class} (and its inner classes) to be included in
     * the analysis.</p>
     *
     * <p>When the {@code source} is empty (eg: all the source files have been filtered out) no include pattern can be
     * generated, therefore the collection of classes won't be filtered at all. This sometimes is not a problem
     * because the analysis task is skipped anyway ({@code source} property is marked as {@code @SkipWhenEmpty}), but this
     * seems not enough to cover all cases. We decided then to enforce an empty collection for {@code classes} when
     * an empty {@code source} is found.</p>
     *
     * </p>This method provides a snippet that will create a custom task checking whther this constraint is valid
     * in a specific project. </p>
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
