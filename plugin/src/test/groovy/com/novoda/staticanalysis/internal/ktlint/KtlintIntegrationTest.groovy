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
    private static final String XML_REPORT_NOT_ENABLED = 'XML report must be enabled. Please make sure to add "CHECKSTYLE" into reports in your Ktlint configuration'

    public static final String DEFAULT_CONFIG = '''
                    ktlint {
                        reporters = [
                            org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN, 
                            org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
                        ]
                        
                        includeVariants { it.name == "debug" }
                    }
                    '''

    public static final String DEFAULT_CONFIG_V9 = '''
                    ktlint {
                        reporters {
                            reporter "plain"
                            reporter "checkstyle"
                        }
                        
                        includeVariants { it.name == "debug" }
                    }
                    '''

    @Parameterized.Parameters(name = '{0} with ktlint {1}')
    static def rules() {
        return [
                [TestProjectRule.forKotlinProject(), '6.2.1', 'ktlintMainCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forAndroidKotlinProject(), '6.2.1', 'ktlintMainCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forKotlinProject(), '6.3.1', 'ktlintMainCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forAndroidKotlinProject(), '6.3.1', 'ktlintMainCheck.txt', DEFAULT_CONFIG],
                // Fails because of Android dependency problem in non-Android project.
                // > Could not generate a decorated class for class org.jlleitschuh.gradle.ktlint.KtlintPlugin.
                //         > com/android/build/gradle/BaseExtension
                // [TestProjectRule.forKotlinProject(), '7.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forAndroidKotlinProject(), '7.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forKotlinProject(), '7.3.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forAndroidKotlinProject(), '7.3.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forKotlinProject(), '8.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forAndroidKotlinProject(), '8.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG],
                [TestProjectRule.forKotlinProject(), '9.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG_V9],
                [TestProjectRule.forAndroidKotlinProject(), '9.0.0', 'ktlintMainSourceSetCheck.txt', DEFAULT_CONFIG_V9],
        ]*.toArray()
    }

    @Rule
    public final TestProjectRule projectRule
    private final String ktlintVersion
    private final String expectedOutputFileName
    private final String defaultConfig

    KtlintIntegrationTest(TestProjectRule projectRule, String ktlintVersion, String expectedOutputFileName, String defaultConfig) {
        this.projectRule = projectRule
        this.ktlintVersion = ktlintVersion
        this.expectedOutputFileName = expectedOutputFileName
        this.defaultConfig = defaultConfig
    }

    @Test
    void shouldNotFailWhenKtlintIsNotConfigured() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainKtlintViolations()
    }

    @Test
    void shouldFailBuildOnConfigurationWhenDetektConfiguredWithoutXmlReport() {
        def result = projectRule.newProject()
                .withPlugin('org.jlleitschuh.gradle.ktlint', ktlintVersion)
                .withToolsConfig('ktlint { }')
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).contains(XML_REPORT_NOT_ENABLED)
    }

    @Test
    void shouldFailBuildOnConfigurationWhenKtlintConfiguredButNotApplied() {
        def result = projectRule.newProject()
                .withToolsConfig(defaultConfig)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).contains(KTLINT_NOT_APPLIED)
    }

    @Test
    void shouldFailBuildWhenKtlintErrorsOverTheThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR)
                .withToolsConfig(defaultConfig)
                .buildAndFail('evaluateViolations')

        assertThat(result.logs).containsLimitExceeded(1, 0)
        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl("reports/ktlint/$expectedOutputFileName"))
    }

    @Test
    void shouldNotFailWhenErrorsAreWithinThreshold() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_WITH_ERROR, 1)
                .withToolsConfig(defaultConfig)
                .build('evaluateViolations')

        assertThat(result.logs).containsKtlintViolations(1,
                result.buildFileUrl("reports/ktlint/$expectedOutputFileName"))
    }

    @Test
    void shouldNotFailBuildWhenNoErrorsEncounteredAndNoThresholdTrespassed() {
        def result = createProjectWith(Fixtures.Ktlint.SOURCES_NO_ERROR, 0)
                .withToolsConfig(defaultConfig)
                .build('evaluateViolations')

        assertThat(result.logs).doesNotContainLimitExceeded()
        assertThat(result.logs).doesNotContainKtlintViolations()
    }

    private TestProject createProjectWith(File sources, int maxErrors = 0) {
        projectRule.newProject()
                .withPlugin('org.jlleitschuh.gradle.ktlint', ktlintVersion)
                .copyIntoSourceSet('main', sources)
                .withPenalty("""{
                    maxWarnings = 0
                    maxErrors = ${maxErrors}
                }""")
    }
}
