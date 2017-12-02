package com.novoda.staticanalysis.internal.checkstyle

import com.google.common.truth.Truth
import com.novoda.test.Fixtures
import com.novoda.test.TestAndroidProject
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

import static com.novoda.test.LogsSubject.assertThat

class CheckstyleAndroidVariantIntegrationTest {

    private static final String DEFAULT_CHECKSTYLE_CONFIG = """
        checkstyle { configFile new File('${Fixtures.Checkstyle.MODULES.path}') }
    """

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    public void shouldNotFailBuildWhenCheckstyleViolationsBelowThresholdInApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_CHECKSTYLE_CONFIG)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsCheckstyleViolations(0, 1,
                result.buildFileUrl('reports/checkstyle/main.html'))
    }

    @Test
    public void shouldFailBuildWhenCheckstyleViolationsOverThresholdInUnitTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('test', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_CHECKSTYLE_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFileUrl('reports/checkstyle/main.html'),
                result.buildFileUrl('reports/checkstyle/test.html'))
    }

    @Test
    public void shouldFailBuildWhenCheckstyleViolationsOverThresholdInAndroidTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('androidTest', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_CHECKSTYLE_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFileUrl('reports/checkstyle/main.html'),
                result.buildFileUrl('reports/checkstyle/androidTest.html'))
    }

    @Test
    public void shouldFailBuildWhenCheckstyleViolationsOverThresholdInActiveProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withSourceSet('full', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''    
                    flavorDimensions 'tier'

                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }

                    variantFilter { variant ->
                        if(variant.name.contains('full')) {
                            variant.setIgnore(true);
                        }
                    }
                ''')
                .withToolsConfig(DEFAULT_CHECKSTYLE_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFileUrl('reports/checkstyle/main.html'),
                result.buildFileUrl('reports/checkstyle/demo.html'))
    }

    @Test
    public void shouldContainCheckstyleTasksForAllVariantsByDefault() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withSourceSet('full', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    flavorDimensions 'tier\'

                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }
                ''')
                .withToolsConfig(DEFAULT_CHECKSTYLE_CONFIG)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(2, 1,
                result.buildFileUrl('reports/checkstyle/main.html'),
                result.buildFileUrl('reports/checkstyle/demo.html'),
                result.buildFileUrl('reports/checkstyle/full.html'))
        Truth.assertThat(result.tasksPaths.findAll { it.startsWith(':checkstyle') }).containsAllIn([
                ":checkstyleDebug",
                ":checkstyleDemo",
                ":checkstyleDemoDebug",
                ":checkstyleDemoRelease",
                ":checkstyleFull",
                ":checkstyleFullDebug",
                ":checkstyleFullRelease",
                ":checkstyleMain",
                ":checkstyleRelease",
                ":checkstyleTest",
                ":checkstyleTestDebug",
                ":checkstyleTestDemo",
                ":checkstyleTestDemoDebug",
                ":checkstyleTestDemoRelease",
                ":checkstyleTestFull",
                ":checkstyleTestFullDebug",
                ":checkstyleTestFullRelease",
                ":checkstyleTestRelease",
                ":checkstyleAndroidTest",
                ":checkstyleAndroidTestDemo",
                ":checkstyleAndroidTestFull"])
    }

    @Test
    public void shouldContainCheckstyleTasksForIncludedVariantsOnly() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withSourceSet('full', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    flavorDimensions 'tier\'

                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }
                ''')
                .withToolsConfig("""
                    checkstyle {
                        configFile new File('${Fixtures.Checkstyle.MODULES.path}')
                        includeVariants { variant -> variant.name.equals('demoDebug') }
                    }
                """)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFileUrl('reports/checkstyle/main.html'),
                result.buildFileUrl('reports/checkstyle/demo.html'))
        def checkstyleTasks = result.tasksPaths.findAll { it.startsWith(':checkstyle') }
        Truth.assertThat(checkstyleTasks).containsAllIn([
                ":checkstyleDebug",
                ":checkstyleDemo",
                ":checkstyleDemoDebug",
                ":checkstyleMain"])
        Truth.assertThat(checkstyleTasks).containsNoneIn([
                ":checkstyleDemoRelease",
                ":checkstyleFull",
                ":checkstyleFullDebug",
                ":checkstyleFullRelease",
                ":checkstyleRelease",
                ":checkstyleTest",
                ":checkstyleTestDebug",
                ":checkstyleTestDemo",
                ":checkstyleTestDemoDebug",
                ":checkstyleTestDemoRelease",
                ":checkstyleTestFull",
                ":checkstyleTestFullDebug",
                ":checkstyleTestFullRelease",
                ":checkstyleTestRelease",
                ":checkstyleAndroidTest",
                ":checkstyleAndroidTestDemo",
                ":checkstyleAndroidTestFull"])
    }

}
