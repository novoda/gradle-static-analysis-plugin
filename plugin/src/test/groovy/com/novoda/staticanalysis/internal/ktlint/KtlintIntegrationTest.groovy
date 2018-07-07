package com.novoda.staticanalysis.internal.ktlint

import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class KtlintIntegrationTest {

    private static final String KTLINT_NOT_APPLIED = 'The Ktlint plugin is configured but not applied. Please apply the plugin in your build script.'

    @Parameterized.Parameters(name = "{0}")
    static def rules() {
        return [TestProjectRule.forAndroidKotlinProject()] // TestProjectRule.forKotlinProject(),
    }

    @Rule
    public final TestProjectRule projectRule

    KtlintIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldNotFailWhenKtlintIsNotConfigured() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainKtlintViolations()
    }

    @Test
    void shouldFailBuildOnConfigurationWhenKtlintConfiguredButNotApplied() {
        def result = projectRule.newProject()
                .withToolsConfig('ktlint {}')
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).contains(KTLINT_NOT_APPLIED)
    }

    @Test
    void shouldFailBuildWhenKtlintErrorsOverTheThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .withToolsConfig('''
                    ktlint {
                        includeVariants { it.name == "debug" }
                    }
                    ''')
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl('reports/ktlint/ktlint-debug.txt'))
    }

    @Test
    void shouldNotFailWhenErrorsAreWithinThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR, 1)
                .withToolsConfig('''
                    ktlint {
                        includeVariants { it.name == "debug" }
                    }
                    ''')
                .build('evaluateViolations')

        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl('reports/ktlint/ktlint-debug.txt'))
    }

    @Test
    void shouldNotFailBuildWhenNoErrorsEncounteredAndNoThresholdTrespassed() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_NO_ERROR, 0)
                .withToolsConfig('ktlint {}')
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainKtlintViolations()
    }

    private TestProject createProjectWith(File sources, int maxErrors = 0) {
        projectRule.newProject()
                .withPlugin("org.jlleitschuh.gradle.ktlint", "4.1.0")
                .withSourceSet('main', sources)
                .withPenalty("""{
                    maxWarnings = 0
                    maxErrors = ${maxErrors}
                }""")
    }
}
