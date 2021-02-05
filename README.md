# [DEPRECATED] Gradle static analysis plugin

> :warning: A fork of this project is maintained at https://github.com/GradleUp/static-analysis-plugin/
> Please migrate by using `com.gradleup.static-analysis` plugin id instead.

[![](https://ci.novoda.com/buildStatus/icon?job=gradle-static-analysis-plugin)](https://ci.novoda.com/job/gradle-static-analysis-plugin/lastSuccessfulBuild) [![](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE.txt) [![Bintray](https://api.bintray.com/packages/novoda-oss/maven/gradle-static-analysis-plugin/images/download.svg)](https://bintray.com/novoda-oss/maven/gradle-static-analysis-plugin/_latestVersion)

A Gradle plugin to easily apply the same setup of static analysis tools across different Android, Java or Kotlin projects.

Supports [Task Configuration Avoidance](https://docs.gradle.org/current/userguide/task_configuration_avoidance.html) so that you have zero overhead in build speeds when you use this plugin!

## Description
Gradle supports many popular static analysis (Checkstyle, PMD, FindBugs, etc) via a set of built-in plugins.
Using these plugins in an Android module will require an additional setup to compensate for the differences between
the model adopted by the Android plugin compared to the the Java one.

The `gradle-static-analysis-plugin` aims to provide:
- flexible, configurable penalty strategy for builds
- easy, Android-friendly integration for all static analysis
- convenient way of sharing same setup across different projects
- healthy, versionable and configurable defaults

### Supported tools
The plugin supports various static analysis tools for Java, Kotlin and Android projects:

 * [`Checkstyle`](docs/tools/checkstyle.md)
 * [`PMD`](docs/tools/pmd.md)
 * [`FindBugs`](docs/tools/findbugs.md) [DEPRECATED] [Removed in Gradle 6.0]
 * [`SpotBugs`](docs/tools/spotbugs.md)
 * [`Detekt`](docs/tools/detekt.md)
 * [`Android Lint`](docs/tools/android_lint.md)
 * [`KtLint`](docs/tools/ktlint.md)
 
Please note that the tools availability depends on the project the plugin is applied to. For more details please refer to the
[supported tools](docs/supported-tools.md) page.

### Tools in-consideration
                          
 * `CPD (Duplicate Code Detection) ` [#150](https://github.com/novoda/gradle-static-analysis-plugin/issues/150)
 * `error-prone` [#151](https://github.com/novoda/gradle-static-analysis-plugin/issues/151)
 * `Jetbrains IDEA Inspections` [#152](https://github.com/novoda/gradle-static-analysis-plugin/issues/152)

For all tools in consideration, please refer to [issues](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aissue+is%3Aopen+label%3A%22new+tool%22). 

### Out-of-the-box support for Android projects
Android projects use a Gradle model that is not compatible with the Java one, supported by the built-in static analysis tools plugins.
Applying `gradle-static-analysis-plugin` to your Android project will make sure all the necessary tasks are created and correctly configured
without any additional hassle.

## Add the plugin to your project
Apply the plugin from jCenter as a classpath dependency

```gradle
buildscript {
    repositories {
       jcenter()
    }
    dependencies {
        classpath 'com.novoda:gradle-static-analysis-plugin:1.2'
    }
}

apply plugin: 'com.novoda.static-analysis'
```
        
or from the [Gradle Plugins Repository](https://plugins.gradle.org/):

```gradle
plugins {
    id 'com.novoda.static-analysis' version '1.2'
}

```

## Simple usage
A typical configuration for the plugin will look like:

```gradle
staticAnalysis {
    penalty {
        maxErrors = 0
        maxWarnings = 0
    }
    checkstyle { }
    pmd { }
    findbugs { }
    spotbugs { }
    detekt { }
    lintOptions { }
}
```

This will enable all the tools with their default settings and create `evaluateViolations` task. Running `./gradlew evaluateViolations` task will run all configured tools and print the reports to console. For more advanced configurations, please refer to the
[advanced usage](docs/advanced-usage.md) and to the [supported tools](docs/supported-tools.md) pages.

## Sample app
There are two sample Android projects available, one consisting of a regular app - available [here](https://github.com/novoda/gradle-static-analysis-plugin/tree/master/sample) - and the other comprising a multi-module setup available [here](https://github.com/novoda/gradle-static-analysis-plugin/tree/master/sample-multi-module). Both sample projects showcase a setup featuring Checkstyle, FindBugs, SpotBugs, PMD, Lint, Ktlint and Detekt.

## Snapshots
[![CI status](https://ci.novoda.com/buildStatus/icon?job=gradle-static-analysis-plugin-snapshot)](https://ci.novoda.com/job/gradle-static-analysis-plugin-snapshot/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda-oss/snapshots/gradle-static-analysis-plugin/images/download.svg)](https://bintray.com/novoda-oss/snapshots/gradle-static-analysis-plugin/_latestVersion)

Snapshot builds from [`develop`](https://github.com/novoda/gradle-static-analysis-plugin/compare/master...develop) are automatically deployed to a [repository](https://bintray.com/novoda-oss/snapshots/gradle-static-analysis-plugin/_latestVersion) that is not synced with JCenter.
To consume a snapshot build add an additional maven repo as follows:
```
repositories {
    maven {
        url 'https://dl.bintray.com/novoda-oss/snapshots/'
    }
}
```

You can find the latest snapshot version following this [link](https://bintray.com/novoda-oss/snapshots/gradle-static-analysis-plugin/_latestVersion).

## Roadmap

This project is routinely used by many Novoda projects and by other external projects with no known critical issues.

Future improvements and new tool integrations can be found on the repository's
[issue tracker](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement).
