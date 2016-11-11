package com.novoda.staticanalysis.internal

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceTask

class SourceFilter {

    private final List<Object> excludes = []

    void exclude(Object rule) {
        excludes.add(rule)
    }

    void applyTo(SourceTask task) {
        excludes.each { filter ->
            if (filter instanceof FileCollection) {
                task.source = task.source.findAll { !filter.contains(it) }
            } else {
                task.exclude(filter as String)
            }
        }
    }

}
