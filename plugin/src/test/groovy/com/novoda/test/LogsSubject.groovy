package com.novoda.test

import com.google.common.truth.FailureStrategy
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.SubjectFactory
import com.google.common.truth.Truth
import com.google.common.truth.TruthJUnit

import javax.annotation.Nullable

import static com.novoda.test.TestProject.Result.Logs;

class LogsSubject extends Subject<LogsSubject, Logs> {

    private static final String VIOLATIONS_LIMIT_EXCEEDED = 'Violations limit exceeded'
    private static final String CHECKSTYLE_VIOLATIONS_FOUND = 'Checkstyle violations found'
    private static final String PMD_VIOLATIONS_FOUND = 'PMD violations found'
    private static final String FINDBUGS_VIOLATIONS_FOUND = 'Findbugs violations found'
    private static final String SPOTBUGS_VIOLATIONS_FOUND = 'SpotBugs violations found'
    private static final String DETEKT_VIOLATIONS_FOUND = 'Detekt violations found'
    private static final String KTLINT_VIOLATIONS_FOUND = 'ktlint violations found'
    private static final String LINT_VIOLATIONS_FOUND = 'Lint violations found'

    private static final SubjectFactory<LogsSubject, Logs> FACTORY = new SubjectFactory<LogsSubject, Logs>() {
        @Override
        LogsSubject getSubject(FailureStrategy failureStrategy, Logs logs) {
            new LogsSubject(failureStrategy, logs)
        }
    }

    public static LogsSubject assertThat(Logs logs) {
        Truth.assertAbout(FACTORY).that(logs)
    }

    public static LogsSubject assumeThat(Logs logs) {
        TruthJUnit.assume().about(FACTORY).that(logs)
    }

    LogsSubject(FailureStrategy failureStrategy, @Nullable Logs actual) {
        super(failureStrategy, actual)
    }

    private StringSubject getOutputSubject() {
        check().that(actual().output)
    }

    public void contains(log) {
        outputSubject.contains(log)
    }

    public void doesNotContainLimitExceeded() {
        outputSubject.doesNotContain(VIOLATIONS_LIMIT_EXCEEDED)
    }

    public void containsLimitExceeded(int errors, int warnings) {
        outputSubject.contains("$VIOLATIONS_LIMIT_EXCEEDED by $errors errors, $warnings warnings.")
    }

    public void doesNotContainCheckstyleViolations() {
        outputSubject.doesNotContain(CHECKSTYLE_VIOLATIONS_FOUND)
    }

    public void doesNotContainPmdViolations() {
        outputSubject.doesNotContain(PMD_VIOLATIONS_FOUND)
    }

    public void doesNotContainFindbugsViolations() {
        outputSubject.doesNotContain(FINDBUGS_VIOLATIONS_FOUND)
    }

    public void doesNotContainSpotBugsViolations() {
        outputSubject.doesNotContain(SPOTBUGS_VIOLATIONS_FOUND)
    }

    public void doesNotContainDetektViolations() {
        outputSubject.doesNotContain(DETEKT_VIOLATIONS_FOUND)
    }

    public void doesNotContainKtlintViolations() {
        outputSubject.doesNotContain(KTLINT_VIOLATIONS_FOUND)
    }

    public void doesNotContainLintViolations() {
        outputSubject.doesNotContain(LINT_VIOLATIONS_FOUND)
    }

    public void containsCheckstyleViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(CHECKSTYLE_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    public void containsPmdViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(PMD_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    public void containsFindbugsViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(FINDBUGS_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    public void containsSpotBugsViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(SPOTBUGS_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    public void containsDetektViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(DETEKT_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    public void containsKtlintViolations(int errors, String... reportUrls) {
        containsToolViolations(KTLINT_VIOLATIONS_FOUND, errors, 0, reportUrls)
    }

    public void containsLintViolations(int errors, int warnings, String... reportUrls) {
        containsToolViolations(LINT_VIOLATIONS_FOUND, errors, warnings, reportUrls)
    }

    private void containsToolViolations(String template, int errors, int warnings, String... reportUrls) {
        outputSubject.contains("$template ($errors errors, $warnings warnings). See the reports at:\n")
        for (String reportUrl : reportUrls) {
            outputSubject.contains(reportUrl)
        }
    }
}
