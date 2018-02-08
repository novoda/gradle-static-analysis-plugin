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

    private static String LINT_CONFIGURATION =
            """
        lintOptions {              
            lintConfig = file("${Fixtures.Lint.RULES}") 
        }
        """

    @Rule
    public
    final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidProject()

    @Test
    void shouldFailBuildWhenLintViolationsOverThresholdInActiveProductFlavorVariant() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Lint.SOURCES_WITH_WARNINGS)
                .withSourceSet('demo', Fixtures.Lint.SOURCES_WITH_ERRORS)
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
                .withToolsConfig(LINT_CONFIGURATION)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(2, 3)
        assertThat(result.logs).containsLintViolations(2, 4,
                'reports/lint-results-demoDebug.html')
    }

    @Test
    void shouldContainCollectLintTasksForAllVariantsByDefault() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
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
                .withToolsConfig(LINT_CONFIGURATION)
                .build('check')

        Truth.assertThat(result.tasksPaths).containsAllOf(
                ":lintDemoDebug",
                ":collectLintDemoDebugViolations",
                ":lintDemoRelease",
                ":collectLintDemoReleaseViolations",
                ":lintFullDebug",
                ":collectLintFullDebugViolations",
                ":lintFullRelease",
                ":collectLintFullReleaseViolations"
        )
    }

    @Test
    void shouldContainCollectLintTasksForIncludedVariantsOnly() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
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
                    lintOptions {              
                        lintConfig = file("${Fixtures.Lint.RULES}")               
                        includeVariants { it.name == 'demoDebug' }
                    }
                """)
                .build('check')

        Truth.assertThat(result.tasksPaths).containsAllOf(
                ":lintDemoDebug",
                ":collectLintDemoDebugViolations")
        Truth.assertThat(result.tasksPaths).containsNoneOf(
                ":lintDemoRelease",
                ":collectLintDemoReleaseViolations",
                ":lintFullDebug",
                ":collectLintFullDebugViolations",
                ":lintFullRelease",
                ":collectLintFullReleaseViolations")
    }

}
