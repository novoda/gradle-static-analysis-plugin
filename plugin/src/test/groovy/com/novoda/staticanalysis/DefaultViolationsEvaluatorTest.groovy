package com.novoda.staticanalysis

import com.novoda.staticanalysis.ViolationsEvaluator.Input
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentCaptor

import static com.google.common.truth.Truth.assertThat
import static org.junit.Assert.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class DefaultViolationsEvaluatorTest {

    private static final String TOOL_NAME = 'SomeTool'

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    private Project project
    private PenaltyExtension penalty
    private Violations violations
    private File reportFile
    private Logger logger = mock(Logger)

    private DefaultViolationsEvaluator evaluator

    @Before
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(temporaryFolder.newFolder())
                .build()
        penalty = new PenaltyExtension()
        penalty.maxErrors = 1
        penalty.maxWarnings = 1
        violations = new Violations(TOOL_NAME)
        reportFile = temporaryFolder.newFile('report.xml')
        evaluator = new DefaultViolationsEvaluator(ReportUrlRenderer.DEFAULT, logger)
    }

    @Test
    void shouldLogViolationsNumberWhenBelowThreshold() {
        violations.addViolations(1, 0, reportFile)

        evaluator.evaluate(input)

        assertThat(warningLog).contains("$TOOL_NAME violations found (1 errors, 0 warnings).")
    }

    @Test
    void shouldNotLogViolationsNumberWhenNoViolations() {
        violations.addViolations(0, 0, reportFile)

        evaluator.evaluate(input)

        assertThat(warningLog).doesNotContain("$TOOL_NAME violations found")
    }

    @Test
    void shouldThrowExceptionWhenViolationsNumberWhenAboveThreshold() {
        violations.addViolations(1, 2, reportFile)

        try {
            evaluator.evaluate(input)
            fail('Exception expected but not thrown')
        } catch (GradleException e) {
            assertThat(e.message).contains('Violations limit exceeded by 0 errors, 1 warnings.')
        }
    }

    private Input getInput() {
        new Input(penalty, violations)
    }

    private String getWarningLog() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String)
        verify(logger).warn(captor.capture())
        captor.getValue()
    }
}
