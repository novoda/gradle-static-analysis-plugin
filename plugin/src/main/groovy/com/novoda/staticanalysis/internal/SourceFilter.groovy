package com.novoda.staticanalysis.internal

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceTask

class SourceFilter {

    private final Project project
    private final List<Object> excludes = []

    SourceFilter(Project project) {
        this.project = project
    }

    void exclude(Object exclude) {
        excludes.add(exclude)
    }

    void applyTo(SourceTask task) {
        excludes.each { exclude ->
            if (exclude instanceof File) {
                apply(task, project.files(exclude))
            } else if (exclude instanceof FileCollection) {
                apply(task, exclude)
            } else if (exclude instanceof Iterable<File>) {
                apply(task, exclude.inject(null, accumulateIntoTree()) as FileTree)
            } else {
                task.exclude(exclude as String)
            }
        }
    }

    private void apply(SourceTask task, FileCollection excludedFiles) {
        task.source = task.source.findAll { !excludedFiles.contains(it) }
    }

    private def accumulateIntoTree() {
        return { tree, file -> tree?.plus(project.fileTree(file)) ?: project.fileTree(file) }
    }

}
