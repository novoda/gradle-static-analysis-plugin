package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.test.Fixtures
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized.class)
class CheckstyleConfigurationTest {

    @Parameterized.Parameters(name = "{0}")
    static Iterable<TestProjectRule> rules() {
        return [
                TestProjectRule.forJavaProject(),
                TestProjectRule.forKotlinProject(),
                TestProjectRule.forAndroidProject(),
                TestProjectRule.forAndroidKotlinProject(),
        ]
    }

    @Rule
    public final TestProjectRule projectRule

    CheckstyleConfigurationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldConfigureSuccessFully() {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withToolsConfig("checkstyle { }")
                .build('check', '--dry-run')
    }

    @Test
    void shouldNotFailBuildWhenCheckstyleIsConfiguredMultipleTimes() {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Checkstyle.SOURCES_WITH_WARNINGS)
                .withToolsConfig("""  
                    checkstyle {
                        configFile new File('${Fixtures.Checkstyle.MODULES.path}') 
                    }
                    checkstyle {
                        ignoreFailures = false
                    }  
                """)
                .build('check', '--dry-run')
    }
}
