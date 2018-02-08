package com.novoda.staticanalysis.internal.lint

import com.google.common.truth.Truth
import com.novoda.test.Fixtures
import com.novoda.test.TestAndroidProject
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test

import static com.novoda.test.LogsSubject.assertThat

class LintAndroidVariantIntegrationTest {

    private static final String LINT_CONFIGURATION =
            """
        lintOptions {       
            lintConfig = file("${Fixtures.Lint.RULES}") 
        }
        """

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    void shouldFailBuildWhenLintViolationsOverThresholdInActiveProductFlavorVariant() {
        TestProject.Result result = starterProject()
                .withSourceSet('demo', Fixtures.Lint.SOURCES_WITH_ERRORS)
                .withToolsConfig(LINT_CONFIGURATION)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(2, 3)
        assertThat(result.logs).containsLintViolations(2, 4,
                'reports/lint-results-demoDebug.html',
                'reports/lint-results-demoRelease.html',
                'reports/lint-results-fullDebug.html',
                'reports/lint-results-fullRelease.html'
        )
    }

    @Test
    void shouldContainCollectLintTasksForAllVariantsByDefault() {
        TestProject.Result result = starterProject()
                .withToolsConfig(LINT_CONFIGURATION)
                .buildAndFail('evaluateViolations')

        Truth.assertThat(result.tasksPaths).containsAllOf(
                ':lintDemoDebug',
                ':collectLintDemoDebugViolations',
                ':lintDemoRelease',
                ':collectLintDemoReleaseViolations',
                ':lintFullDebug',
                ':collectLintFullDebugViolations',
                ':lintFullRelease',
                ':collectLintFullReleaseViolations'
        )

        Truth.assertThat(result.tasksPaths).doesNotContain(':lint')
    }

    @Test
    void shouldContainCollectLintTasksForIncludedVariantsOnly() {
        TestProject.Result result = starterProject()
                .withToolsConfig("""
                    lintOptions {        
                        checkReleaseBuilds false      
                        lintConfig = file("${Fixtures.Lint.RULES}")               
                        includeVariants { it.name == 'demoDebug' }
                    }
                """)
                .build('evaluateViolations')

        Truth.assertThat(result.tasksPaths).containsAllOf(
                ':lintDemoDebug',
                ':collectLintDemoDebugViolations')
        Truth.assertThat(result.tasksPaths).containsNoneOf(
                ':lint',
                ':lintDemoRelease',
                ':collectLintDemoReleaseViolations',
                ':lintFullDebug',
                ':collectLintFullDebugViolations',
                ':lintFullRelease',
                ':collectLintFullReleaseViolations')
    }

    private TestAndroidProject starterProject() {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Lint.SOURCES_WITH_WARNINGS)
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
                ''')
    }

}
