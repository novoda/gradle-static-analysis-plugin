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
    public static final String DEFAULT_CONFIG = '''
                    ktlint {
                        includeVariants { it.name == "debug" }
                    }
                    '''
    public static final String EMPTY_CONFIG = 'ktlint {}'

    @Parameterized.Parameters(name = '{0}')
    static def rules() {
        return [
                [TestProjectRule.forKotlinProject(), 'main', '4.1.0'].toArray(),
                [TestProjectRule.forAndroidKotlinProject(), 'debug', '4.1.0'].toArray(),
                [TestProjectRule.forKotlinProject(), 'main', '5.1.0'].toArray(),
                [TestProjectRule.forAndroidKotlinProject(), 'debug', '5.1.0'].toArray(),
                [TestProjectRule.forKotlinProject(), 'main', '6.3.0'].toArray(),
                [TestProjectRule.forAndroidKotlinProject(), 'debug', '6.3.0'].toArray(),
        ]
    }

    @Rule
    public final TestProjectRule projectRule
    private final String sourceSetName
    private final String ktlintVersion

    KtlintIntegrationTest(TestProjectRule projectRule, String sourceSetName, String ktlintVersion) {
        this.projectRule = projectRule
        this.sourceSetName = sourceSetName
        this.ktlintVersion = ktlintVersion
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
                .withToolsConfig(EMPTY_CONFIG)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).contains(KTLINT_NOT_APPLIED)
    }

    @Test
    void shouldFailBuildWhenKtlintErrorsOverTheThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .withToolsConfig(DEFAULT_CONFIG)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl("reports/ktlint/ktlint-${sourceSetName}.txt"))
    }

    @Test
    void shouldNotFailWhenErrorsAreWithinThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR, 1)
                .withToolsConfig(DEFAULT_CONFIG)
                .build('evaluateViolations')

        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl("reports/ktlint/ktlint-${sourceSetName}.txt"))
    }

    @Test
    void shouldNotFailBuildWhenNoErrorsEncounteredAndNoThresholdTrespassed() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_NO_ERROR, 0)
                .withToolsConfig(EMPTY_CONFIG)
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainKtlintViolations()
    }

    private TestProject createProjectWith(File sources, int maxErrors = 0) {
        projectRule.newProject()
                .withPlugin('org.jlleitschuh.gradle.ktlint', ktlintVersion)
                .withSourceSet('main', sources)
                .withPenalty("""{
                    maxWarnings = 0
                    maxErrors = ${maxErrors}
                }""")
    }
}
