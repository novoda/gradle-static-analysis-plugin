package com.novoda.staticanalysis.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.util.GradleVersion

class TasksCompat {

    private static boolean IS_GRADLE_MIN_49 = GradleVersion.current() >= GradleVersion.version('4.9')

    static <T extends Task> Object createTask(Project project, String name, Class<T> type, Action<? super T> configuration) {
        if (IS_GRADLE_MIN_49) {
            return project.tasks.register(name, type, configuration)
        } else {
            return project.tasks.create(name, type, configuration)
        }
    }

    static <T extends Task> void configureEach(TaskCollection<T> tasks, Action<? super T> configuration) {
        if (IS_GRADLE_MIN_49) {
            tasks.configureEach(configuration)
        } else {
            tasks.all(configuration)
        }
    }

    static <T extends Task> void configureNamed(Project project, String taskName, Action<? super T> configuration) {
        if (IS_GRADLE_MIN_49) {
            project.tasks.named(taskName).configure(configuration)
        } else {
            project.tasks.getByName(taskName).configure(configuration)
        }
    }

}
