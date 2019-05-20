# ktlint
[Ktlint](https://github.com/shyiko/ktlint) is a linter for Kotlin with a built-in formatter. It does not support Java. Adding 
this tool only makes sense when you have Kotlin sources in your project. 

> Supported Ktlint Gradle Plugin version: **6.2.1 and above** 

## Table of contents
 * [IMPORTANT: setup Ktlint](#important-setup-ktlint)
 * [Configure Ktlint](#configure-ktlint)
---

## IMPORTANT: setup Ktlint

Unlike the other tools, the plugin **won't automatically add Ktlint** to your project. If you forget to do it, the plugin will 
fail the build with an error.

In order to integrate Ktlint easily we choose to use the [Ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle/).
This plugin has a very good understanding of Android source sets and build flavors. You can refer to the
[official documentation](https://github.com/JLLeitschuh/ktlint-gradle/#how-to-use) for further details.

Note that you should _not_ add the `ktlint` closure to your `build.gradle`s, unlike what the official documentation says. The 
`ktlint` closure in the `staticAnalysis` configuration gets applied to all Kotlin modules automatically.

In most common cases, adding Ktlint to a project boils down to these simple steps:

 1. Add this statement to your root `build.gradle` project (change the version according to your needs):
    ```gradle
    plugins {
        id 'org.jlleitschuh.gradle.ktlint' version '7.3.0'
        // ...
    }
    ```
 2. Add this statement to each Kotlin project's `build.gradle`s:
    ```gradle
    plugins {
        id 'org.jlleitschuh.gradle.ktlint'
        // ...
    }
    ```
    
## Configure Ktlint

Unlike other tools, Ktlint does not offer much configuration. By default, it applies 
[Kotlin style guide](https://kotlinlang.org/docs/reference/coding-conventions.html) or 
[Android Kotlin style guide](https://android.github.io/kotlin-guides/style.html).

To use Android style guide: 

```gradle
ktlint {
    android true
}
```

For other configuration options and adding custom rules, refer to the 
[official guide](https://github.com/JLLeitschuh/ktlint-gradle/#configuration).

**Note:** Failures and threshold detection is handled by Static Analysis plugin. That is why `ignoreFailures = true` is set by 
the plugin. Please do not manually override `ignoreFailures` property. 
