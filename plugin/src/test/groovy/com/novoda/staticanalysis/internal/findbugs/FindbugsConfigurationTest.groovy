package com.novoda.staticanalysis.internal.findbugs


import com.novoda.test.TestProjectRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.novoda.test.Fixtures.Findbugs.SOURCES_WITH_LOW_VIOLATION

@RunWith(Parameterized.class)
class FindbugsConfigurationTest {

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

    FindbugsConfigurationTest(TestProjectRule projectRule) {
        this.projectRule = projectRule
    }

    @Test
    void shouldConfigureSuccessFully() {
        projectRule.newProject()
                .withSourceSet('main', SOURCES_WITH_LOW_VIOLATION)
                .withToolsConfig("findbugs { }")
                .build('check', '--dry-run')
    }

    @Test
    void shouldNotFailBuildWhenFindbugsIsConfiguredMultipleTimes() {
        projectRule.newProject()
                .withSourceSet('main', SOURCES_WITH_LOW_VIOLATION)
                .withToolsConfig("""
                    findbugs { }
                    findbugs {
                        ignoreFailures = false
                    }
                """)
                .build('check', '--dry-run')
    }
}
