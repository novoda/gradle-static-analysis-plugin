package com.novoda.staticanalysis

import com.novoda.test.DeployRulesTestRule
import com.novoda.test.Fixtures
import com.novoda.test.TestProject
import com.novoda.test.TestProjectRule
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.LogsSubject.assertThat

@RunWith(Parameterized.class)
class RulesIntegrationTest {

    @ClassRule
    public static final DeployRulesTestRule DEPLOY_RULES = new DeployRulesTestRule(
            resourceDirs: [Fixtures.RULES_DIR],
            repoDir: new File(Fixtures.BUILD_DIR, 'rules'))

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<TestProjectRule> rules() {
        return [TestProjectRule.forJavaProject(), TestProjectRule.forAndroidProject()]
    }

    @Rule
    public final TestProjectRule projectRule

    RulesIntegrationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    public void shouldUseConfigurationFromArtifactWhenProvided() {
        TestProject.Result result = projectRule.newProject()
                .withAdditionalConfiguration("repositories { maven { url '$DEPLOY_RULES.repoDir' } }")
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withPenalty('''{
                    maxWarnings = 0
                    maxErrors = 0
                }''')
                .withToolsConfig("""
                    rules {
                        novoda { maven '$DEPLOY_RULES.mavenCoordinates' }
                    }
                    checkstyle {
                       config rules.novoda['checkstyle/config/modules.xml']
                    }
                """)
                .buildAndFail('check')

        assertThat(result.logs).containsLimitExceeded(0, 1)
        assertThat(result.logs).containsCheckstyleViolations(0, 1,
                result.buildFileUrl('reports/checkstyle/main.html'))
    }

}
