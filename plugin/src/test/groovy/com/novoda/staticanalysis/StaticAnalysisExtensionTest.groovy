package com.novoda.staticanalysis

import com.novoda.staticanalysis.ViolationsEvaluator.Input
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static com.google.common.truth.Truth.assertThat
import static org.junit.Assert.fail

class StaticAnalysisExtensionTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    private Project project
    private StaticAnalysisExtension extension

    @Before
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(temporaryFolder.newFolder())
                .build()

        extension = new StaticAnalysisExtension(project)
        extension.allViolations.create('SomeTool') {
            it.addViolations(2, 2, temporaryFolder.newFile())
        }
    }

    @Test
    void shouldOverrideEvaluatorWhenEvaluationLogicProvided() {
        def e = new GradleException()
        extension.evaluator { throw e }

        try {
            extension.evaluator.evaluate(extension.evaluatorInput)

            fail('Exception expected but not thrown')
        } catch (Exception thrown) {
            assertThat(thrown).isEqualTo(e)
        }
    }

    @Test
    void shouldProvidePenaltyFromExtensionToDefineCustomEvaluator() {
        Input capturedInput = null
        extension.evaluator { input ->
            capturedInput = input
        }

        extension.evaluator.evaluate(extension.evaluatorInput)

        assertThat(capturedInput.penalty).isEqualTo(extension.penalty)
    }

    @Test
    void shouldProvideViolationsFromExtensionToDefineCustomEvaluator() {
        Input capturedInput = null
        extension.evaluator { input ->
            capturedInput = input
        }

        extension.evaluator.evaluate(extension.evaluatorInput)

        assertThat(capturedInput.allViolations).containsAllIn(extension.allViolations)
    }
}