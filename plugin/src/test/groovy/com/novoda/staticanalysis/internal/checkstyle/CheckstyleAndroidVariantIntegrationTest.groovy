package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.test.Fixtures
import com.novoda.test.TestAndroidProject
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

import static com.novoda.test.LogsSubject.assertThat

class CheckstyleAndroidVariantIntegrationTest {

    private static final String DEFAULT_CONFIG = "configFile new File('${Fixtures.Checkstyle.MODULES.path}')"

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    public void shouldFailBuildWhenCheckstyleViolationsOverThresholdInMainApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsCheckstyleViolations(0, 1,
                result.buildFile('reports/checkstyle/main.html'))
    }

    @Test
    public void shouldNotFailBuildWhenCheckstyleViolationsBelowThresholdInMainApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsCheckstyleViolations(0, 1,
                result.buildFile('reports/checkstyle/main.html'))
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
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFile('reports/checkstyle/main.html'),
                result.buildFile('reports/checkstyle/test.html'))
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
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFile('reports/checkstyle/main.html'),
                result.buildFile('reports/checkstyle/androidTest.html'))
    }

    @Test
    public void shouldFailBuildWhenCheckstyleViolationsOverThresholdInProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withSourceSet('full', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                    }
                ''')
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFile('reports/checkstyle/main.html'),
                result.buildFile('reports/checkstyle/demo.html'))
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
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsCheckstyleViolations(1, 1,
                result.buildFile('reports/checkstyle/main.html'),
                result.buildFile('reports/checkstyle/demo.html'))
    }

    @Test
    public void shouldNotFailBuildWhenCheckstyleViolationsOverThresholdInIgnoredVariants() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withSourceSet('full', Fixtures.Checkstyle.SOURCES_WITH_ERRORS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                        full
                    }

                    variantFilter { variant ->
                        variant.setIgnore(true);
                    }
                ''')
                .withCheckstyle(checkstyle(DEFAULT_CONFIG))
                .build('check')

        assertThat(result.logs).doesNotContainCheckstyleViolations()
    }

    private static String checkstyle(String configFile, String... configs) {
        """checkstyle {
            ${configFile}
            ${configs.join('\n\t\t\t')}
        }"""
    }
}
