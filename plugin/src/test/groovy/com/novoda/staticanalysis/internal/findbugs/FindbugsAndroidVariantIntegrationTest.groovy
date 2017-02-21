package com.novoda.staticanalysis.internal.findbugs

import com.google.common.truth.Truth
import com.novoda.test.Fixtures
import com.novoda.test.TestAndroidProject
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

import static com.novoda.test.LogsSubject.assertThat

class FindbugsAndroidVariantIntegrationTest {

    private static final String DEFAULT_FINDBUGS_CONFIG = "findbugs {}"

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    public void shouldNotFailBuildWhenFindbugsViolationsBelowThresholdInDefaultApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 2
                }''')
                .withToolsConfig(DEFAULT_FINDBUGS_CONFIG)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(0, 2,
                result.buildFileUrl('reports/findbugs/debug.html'),
                result.buildFileUrl('reports/findbugs/release.html'))
    }


    @Test
    public void shouldFailBuildWhenFindbugsViolationsOverThresholdInUnitTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('test', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 2
                }''')
                .withToolsConfig(DEFAULT_FINDBUGS_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(2, 0)
        assertThat(result.logs).containsFindbugsViolations(2, 2,
                result.buildFileUrl('reports/findbugs/debug.html'),
                result.buildFileUrl('reports/findbugs/release.html'),
                result.buildFileUrl('reports/findbugs/debugUnitTest.html'),
                result.buildFileUrl('reports/findbugs/releaseUnitTest.html'))
    }

    @Test
    public void shouldFailBuildWhenFindbugsViolationsOverThresholdInAndroidTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('androidTest', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 2
                }''')
                .withToolsConfig(DEFAULT_FINDBUGS_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsFindbugsViolations(1, 2,
                result.buildFileUrl('reports/findbugs/debug.html'),
                result.buildFileUrl('reports/findbugs/release.html'),
                result.buildFileUrl('reports/findbugs/debugAndroidTest.html'))
    }

    @Test
    public void shouldFailBuildWhenFindbugsViolationsOverThresholdInActiveProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('demo', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('full', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 2
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                        full
                    }

                    variantFilter { variant ->
                        if(variant.name.contains('full')) {
                            variant.setIgnore(true);
                        }
                    }
                ''')
                .withToolsConfig(DEFAULT_FINDBUGS_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(2, 0)
        assertThat(result.logs).containsFindbugsViolations(2, 2,
                result.buildFileUrl('reports/findbugs/demoDebug.html'),
                result.buildFileUrl('reports/findbugs/demoRelease.html'))
    }

    @Test
    public void shouldContainFindbugsTasksForAllVariantsByDefault() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('demo', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('full', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                        full
                    }
                ''')
                .withToolsConfig(DEFAULT_FINDBUGS_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(3, 3)
        assertThat(result.logs).containsFindbugsViolations(4, 4,
                result.buildFileUrl('reports/findbugs/demoDebug.html'),
                result.buildFileUrl('reports/findbugs/demoRelease.html'),
                result.buildFileUrl('reports/findbugs/fullDebug.html'),
                result.buildFileUrl('reports/findbugs/fullRelease.html'))
        Truth.assertThat(result.tasksPaths.findAll { it.startsWith(':findbugs') }).containsAllIn([
                ":findbugsDemoDebug",
                ":findbugsDemoDebugUnitTest",
                ":findbugsDemoDebugAndroidTest",
                ":findbugsDemoRelease",
                ":findbugsDemoReleaseUnitTest",
                ":findbugsFullDebug",
                ":findbugsFullDebugUnitTest",
                ":findbugsFullRelease",
                ":findbugsFullReleaseUnitTest"])
    }

    @Test
    public void shouldContainFindbugsTasksForIncludedVariantsOnly() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION)
                .withSourceSet('demo', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withSourceSet('full', Fixtures.Findbugs.SOURCES_WITH_HIGH_VIOLATION)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                        full
                    }
                ''')
                .withToolsConfig("""
                    findbugs {
                        includeVariants { variant -> variant.name.equals('demoDebug') }
                    }
                """)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsFindbugsViolations(1, 1,
                result.buildFileUrl('reports/findbugs/demoDebug.html'))
        def findbugsTasks = result.tasksPaths.findAll { it.startsWith(':findbugs') }
        Truth.assertThat(findbugsTasks).containsAllIn([":findbugsDemoDebug"])
        Truth.assertThat(findbugsTasks).containsNoneIn([
                ":findbugsDemoDebugUnitTest",
                ":findbugsDemoDebugAndroidTest",
                ":findbugsDemoRelease",
                ":findbugsDemoReleaseUnitTest",
                ":findbugsFullDebug",
                ":findbugsFullDebugUnitTest",
                ":findbugsFullRelease",
                ":findbugsFullReleaseUnitTest"])
    }

}
