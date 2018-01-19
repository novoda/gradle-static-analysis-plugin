package com.novoda.staticanalysis.internal.pmd

import com.google.common.truth.Truth
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
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/demo.html'))
    }

    @Test
    public void shouldContainPmdTasksForAllVariantsByDefault() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('demo', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withSourceSet('full', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    flavorDimensions 'tier'
                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }
                ''')
                .withToolsConfig(DEFAULT_PMD_RULES)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsPmdViolations(2, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/demo.html'),
                result.buildFileUrl('reports/pmd/full.html'))
        Truth.assertThat(result.tasksPaths.findAll { it.startsWith(':pmd') }).containsAllIn([
                ":pmdDebug",
                ":pmdDemo",
                ":pmdDemoDebug",
                ":pmdDemoRelease",
                ":pmdFull",
                ":pmdFullDebug",
                ":pmdFullRelease",
                ":pmdMain",
                ":pmdRelease",
                ":pmdTest",
                ":pmdTestDebug",
                ":pmdTestDemo",
                ":pmdTestDemoDebug",
                ":pmdTestDemoRelease",
                ":pmdTestFull",
                ":pmdTestFullDebug",
                ":pmdTestFullRelease",
                ":pmdTestRelease",
                ":pmdAndroidTest",
                ":pmdAndroidTestDemo",
                ":pmdAndroidTestFull"])
    }

    @Test
    public void shouldContainPmdTasksForIncludedVariantsOnly() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_3_VIOLATION)
                .withSourceSet('demo', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withSourceSet('full', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withPenalty('''{
                    maxErrors = 1
                    maxWarnings = 1
                }''')
                .withAdditionalAndroidConfig('''
                    flavorDimensions 'tier'
                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }
                ''')
                .withToolsConfig("""
                    pmd {
                        ruleSetFiles = project.files('${Fixtures.Pmd.RULES.path}')
                        includeVariants { variant -> variant.name.equals('demoDebug') }
                    }
                """)
                .build('check')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).containsPmdViolations(1, 1,
                result.buildFileUrl('reports/pmd/main.html'),
                result.buildFileUrl('reports/pmd/demo.html'))
        def pmdTasks = result.tasksPaths.findAll { it.startsWith(':pmd') }
        Truth.assertThat(pmdTasks).containsAllIn([
                ":pmdDebug",
                ":pmdDemo",
                ":pmdDemoDebug",
                ":pmdMain"])
        Truth.assertThat(pmdTasks).containsNoneIn([
                ":pmdDemoRelease",
                ":pmdFull",
                ":pmdFullDebug",
                ":pmdFullRelease",
                ":pmdRelease",
                ":pmdTest",
                ":pmdTestDebug",
                ":pmdTestDemo",
                ":pmdTestDemoDebug",
                ":pmdTestDemoRelease",
                ":pmdTestFull",
                ":pmdTestFullDebug",
                ":pmdTestFullRelease",
                ":pmdTestRelease",
                ":pmdAndroidTest",
                ":pmdAndroidTestDemo",
                ":pmdAndroidTestFull"])
    }

}
