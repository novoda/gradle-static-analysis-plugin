package com.novoda.staticanalysis.internal

import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static com.google.common.truth.Truth.assertThat
import static com.novoda.test.Fixtures.Checkstyle.SOURCES_WITH_ERRORS
import static com.novoda.test.Fixtures.Checkstyle.SOURCES_WITH_WARNINGS

class SourceFilterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    private Project project
    private SourceFilter filter

    @Before
    public void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(temporaryFolder.newFolder())
                .build()
        filter = new SourceFilter()
    }

    @Test
    public void shouldKeepAllSourcesWhenNoExcludeFilterProvided() {
        SourceTask task = givenTaskWith(errorsSources + warningsSources)

        filter.applyTo(task)

        assertThat(task.source).containsExactlyElementsIn(errorsSources + warningsSources)
    }

    @Test
    public void shouldRemoveFilesMatchingThePatternWhenExcludePatternProvided() {
        SourceTask task = givenTaskWith(errorsSources + warningsSources)
        filter.exclude('**/*.java')

        filter.applyTo(task)

        assertThat(task.source).isEmpty()
    }

    @Test
    public void shouldRemoveFilesInSpecifiedFileCollectionWhenExcludeFileCollectionProvided() {
        SourceTask task = givenTaskWith(errorsSources + warningsSources)
        filter.exclude(project.fileTree(SOURCES_WITH_ERRORS))

        filter.applyTo(task)

        assertThat(task.source).containsExactlyElementsIn(warningsSources)
    }

    @Test
    public void shouldRemoveFilesInSpecifiedFileCollectionWhenExcludeSourceSetProvided() {
        project.apply plugin: 'java'
        project.sourceSets.main {
            java {
                srcDir project.file(SOURCES_WITH_ERRORS)
            }
        }
        SourceTask task = givenTaskWith(project.sourceSets.main.java.srcDirs)
        filter.exclude(project.fileTree(SOURCES_WITH_ERRORS))

        filter.applyTo(task)

        assertThat(task.source).isEmpty()
    }

    private SourceTask givenTaskWith(Iterable<File> files) {
        project.tasks.create('someSourceTask', SourceTask) {
            source = project.files(files)
        }
    }

    private Set<File> getErrorsSources() {
        project.fileTree(SOURCES_WITH_ERRORS).files
    }

    private Set<File> getWarningsSources() {
        project.fileTree(SOURCES_WITH_WARNINGS).files
    }
}
