package com.novoda.staticanalysis.internal.pmd

import com.novoda.test.Fixtures
import com.novoda.test.TestAndroidProject
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

import static com.novoda.test.LogsSubject.assertThat

class PmdAndroidVariantIntegrationTest {

    private static final String DEFAULT_PMD_RULES = "pmd { ruleSetFiles = project.files('${Fixtures.Pmd.RULES.path}') }"

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    public void shouldFailBuildWhenPmdViolationsOverThresholdInMainApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 0
                }''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsPmdViolations(0, 1,
                result.buildFileUrl('reports/pmd/main.html'))
    }

    @Test
    public void shouldNotFailBuildWhenPmdViolationsBelowThresholdInMainApplicationVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsPmdViolations(0, 1,
                result.buildFileUrl('reports/pmd/main.html'))
    }

    @Test
    public void shouldFailBuildWhenPmdViolationsOverThresholdInUnitTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('test', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/test.html'))
    }

    @Test
    public void shouldFailBuildWhenPmdViolationsOverThresholdInAndroidTestVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('androidTest', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/androidTest.html'))
    }

    @Test
    public void shouldFailBuildWhenPmdViolationsOverThresholdInProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('demo', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withSourceSet('full', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    productFlavors {
                        demo
                    }
                ''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/demo.html'))
    }

    @Test
    public void shouldFailBuildWhenPmdViolationsOverThresholdInActiveProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('demo', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withSourceSet('full', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
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
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/demo.html'))
    }

    @Test
    public void shouldNotFailBuildWhenPmdViolationsOverThresholdInIgnoredVariants() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('demo', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withSourceSet('full', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
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
                .withToolsConfig(DEFAULT_PMD_RULES)
                .build('check')

        assertThat(result.logs).doesNotContainCheckstyleViolations()
    }

}
