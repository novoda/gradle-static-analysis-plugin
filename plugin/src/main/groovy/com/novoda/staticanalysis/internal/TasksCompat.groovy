package com.novoda.staticanalysis.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskCollection
import org.gradle.util.GradleVersion

class TasksCompat {

    private static boolean IS_GRADLE_MIN_49 = GradleVersion.current() >= GradleVersion.version("4.9")

    static <T extends Task> Object maybeCreateTask(Project project, String name, Class<T> type, Action<? super T> configuration) {
        if (IS_GRADLE_MIN_49) {
            try {
                def provider = project.tasks.named(name)
                provider.configure(configuration)
                return provider
            } catch (UnknownTaskException ignored) {
                return project.tasks.register(name, type, configuration)
            }
        } else {
            return project.tasks.maybeCreate(name, type).configure(configuration)
        }
    }

    static <T extends Task> void configureEach(TaskCollection<T> tasks, Action<? super T> configuration) {
        if (IS_GRADLE_MIN_49) {
            tasks.all(configuration)
        } else {
            tasks.all(configuration)
        }
    }

}
