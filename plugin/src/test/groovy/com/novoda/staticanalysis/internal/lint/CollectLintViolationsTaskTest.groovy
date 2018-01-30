package com.novoda.staticanalysis.internal.lint

import com.google.common.truth.Truth
import com.novoda.staticanalysis.internal.Violations
import com.novoda.test.Fixtures
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class CollectLintViolationsTaskTest {

    @Test
    void shouldAddResultsToViolations() throws Exception {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('collectLintViolationsTask', type: CollectLintViolationsTask)

        Violations violations = new Violations("Android Lint")
        task.collectViolations(Fixtures.Lint.SAMPLE_REPORT, null, violations)

        Truth.assertThat(violations.getErrors()).isEqualTo(1)
        Truth.assertThat(violations.getWarnings()).isEqualTo(1)
    }
}