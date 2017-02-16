package com.novoda.staticanalysis.internal

import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class LogsConfigurationIntegrationTest {

    private static
    final String DEFAULT_CHECKSTYLE_CONFIG = "checkstyle { configFile new File('${Fixtures.Checkstyle.MODULES.path}') }"

    @Parameterized.Parameters
    public static List<Object[]> rules() {
        return [TestProjectRule.forJavaProject(), TestProjectRule.forAndroidProject()].collect { [it] as Object[] }
    }

    @Rule
    public final TestProjectRule projectRule

    public LogsConfigurationIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    public void shouldUseCustomReportUrlRendererWhenProvided() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig("""
                    $DEFAULT_CHECKSTYLE_CONFIG
                    logs {
                        reportUrlRenderer { report -> "**\${report.path}**" }
                    }
                """)
                .build('check')

        assertThat(result.logs).containsCheckstyleViolations(0, 1,
                "**${result.buildFileUrl('reports/checkstyle/main.html')}**")
    }

    @Test
    public void shouldUseDifferentBaseReportUrlWhenProvided() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig("""
                    $DEFAULT_CHECKSTYLE_CONFIG
                    logs {
                        baseReportUrl "what://foo/bar/reports"
                    }
                """)
                .build('check')

        assertThat(result.logs).containsCheckstyleViolations(0, 1, 'what://foo/bar/reports/checkstyle/main.html')
    }

    @Test
    public void shouldMatchDifferentBaseReportUrlWhenProvided() {
        TestProject.Result result = projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxErrors = 0
                    maxWarnings = 1
                }''')
                .withToolsConfig("""
                    $DEFAULT_CHECKSTYLE_CONFIG
                    logs {
                        baseReportUrl "what://foo/bar", "\${project.buildDir}/reports"
                    }
                """)
                .build('check')

        assertThat(result.logs).containsCheckstyleViolations(0, 1, 'what://foo/bar/checkstyle/main.html')
    }
}
