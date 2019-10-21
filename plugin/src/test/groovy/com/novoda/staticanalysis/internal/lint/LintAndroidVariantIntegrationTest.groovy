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

    @Rule
    public final TestProjectRule<TestAndroidProject> projectRule = TestProjectRule.forAndroidLintProject()

    @Test
    void shouldFailBuildWhenLintViolationsOverThreshold() {
        TestProject.Result result = starterProject()
                .withSourceSet('demo', Fixtures.Lint.SOURCES_WITH_ERRORS)
                .withToolsConfig(DEFAULT_CONFIG)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(1, 1)
        assertThat(result.logs).containsLintViolations(1, 1,
                'reports/lint-results.html')
    }

    @Test
    void givenVariantsFilteredShouldFailBuildWithDuplicatedNumbers() {
        TestProject.Result result = starterProject()
                .withSourceSet('demo', Fixtures.Lint.SOURCES_WITH_ERRORS)
                .withToolsConfig(configWithVariants('demoDebug', 'fullRelease'))
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(1, 2)
        assertThat(result.logs).containsLintViolations(1, 2,
                'reports/lint-results-demoDebug.html',
                'reports/lint-results-fullRelease.html'
        )
    }

    @Test
    void shouldIgnoreErrorsFromInactiveVariant() {
        TestProject.Result result = starterProject(maxWarnings: 1)
                .withSourceSet('demo', Fixtures.Lint.SOURCES_WITH_ERRORS)
                .withToolsConfig(configWithVariants('fullRelease'))
                .build('evaluateViolations')

        assertThat(result.logs).containsLintViolations(0, 1)
    }

    @Test
    void shouldContainCollectLintTasks() {
        TestProject.Result result = starterProject(maxWarnings: 1)
                .withToolsConfig(DEFAULT_CONFIG)
                .build('evaluateViolations')

        Truth.assertThat(result.tasksPaths).containsAllOf(
                ':lint',
                ':collectLintViolations',
        )
    }

    @Test
    void givenVariantsFilteredShouldContainTasksForIncludedVariantsOnly() {
        TestProject.Result result = starterProject(maxWarnings: 1)
                .withToolsConfig(configWithVariants('demoDebug'))
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

    private static final String DEFAULT_CONFIG =
            """
        lintOptions {       
            lintConfig = file("${Fixtures.Lint.RULES}") 
        }
        """

    private static configWithVariants(String... variantNames) {
        def commaSeparatedVariants = variantNames.collect { "'$it'" }.join(', ')
        """
            lintOptions {        
                checkReleaseBuilds false      
                lintConfig = file("${Fixtures.Lint.RULES}")               
                includeVariants { it.name in [$commaSeparatedVariants] }
            }
        """
    }

    private TestAndroidProject starterProject(def args = [:]) {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Lint.SOURCES_WITH_WARNINGS)
                .withPenalty("""{
                    maxErrors = ${args.maxErrors ?: 0}
                    maxWarnings = ${args.maxWarnings ?: 0}
                }""")
                .withAdditionalAndroidConfig('''
                    flavorDimensions 'tier'

                    productFlavors {
                        demo { dimension 'tier' }
                        full { dimension 'tier' }
                    }
                ''')
    }
}
