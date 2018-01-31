# Android Lint
[Android Lint](https://developer.android.com/studio/write/lint.html) is a code scanning tool with focus on Android. Besides code it also analyses resources
and configuration files.

## Configure Android Lint
Lint is configured through the `lintOptions` closure. It supports all [official](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.LintOptions.html)
properties except `abortOnError`, `htmlReport` and `xmlReport`. These are hardcoded so lint won't break the build by it's own and always generates reports.
