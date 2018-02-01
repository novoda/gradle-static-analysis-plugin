# Android Lint
[Android Lint](https://developer.android.com/studio/write/lint.html) is a linter and static analysis tool for Android projects which can detect bugs
and potential issues in code, resources and configuration files.

Be aware that Lint just supports Kotlin since version 3.1.0 of the [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin.html).

## Configure Android Lint
Lint is configured through the `lintOptions` closure. It supports all [official](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.LintOptions.html)
properties except `abortOnError`, `htmlReport` and `xmlReport`. These are hardcoded so Lint won't break the build on its own and always generates reports.
