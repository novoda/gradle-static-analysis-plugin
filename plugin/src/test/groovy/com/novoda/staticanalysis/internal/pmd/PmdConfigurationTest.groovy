package com.novoda.staticanalysis.internal.pmd

import com.novoda.test.Fixtures
import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized.class)
class PmdConfigurationTest {

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

    PmdConfigurationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldConfigureSuccessFully() {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withToolsConfig("pmd { }")
                .build('check', '--dry-run')
    }

    @Test
    void shouldNotFailBuildWhenPmdIsConfiguredMultipleTimes() {
        projectRule.newProject()
                .withSourceSet('main', Fixtures.Pmd.SOURCES_WITH_PRIORITY_1_VIOLATION)
                .withToolsConfig("""  
                    pmd {
                    }
                    pmd {
                        ignoreFailures = false
                    }  
                """)
                .build('check', '--dry-run')
    }
}
