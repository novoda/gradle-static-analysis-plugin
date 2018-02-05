# Gradle static analysis plugin
[![](https://ci.novoda.com/buildStatus/icon?job=gradle-static-analysis-plugin)](https://ci.novoda.com/job/gradle-static-analysis-plugin/lastSuccessfulBuild) [![](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE.txt) [![Bintray](https://api.bintray.com/packages/novoda/maven/gradle-static-analysis-plugin/images/download.svg)](https://bintray.com/novoda/maven/gradle-static-analysis-plugin/_latestVersion)

A Gradle plugin to easily apply the same setup of static analysis tools across different Android, Java or Kotlin projects.

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

 * [`Checkstyle`](https://checkstyle.sourceforge.net)
 * [`PMD`](https://pmd.github.io)
 * [`FindBugs`](http://findbugs.sourceforge.net/)
 * [`Detekt`](https://github.com/arturbosch/detekt)
 * [`Android Lint`](https://developer.android.com/studio/write/lint.html)

Support for additional tools is planned but not available yet:

 * [`KtLint`](https://github.com/shyiko/ktlint)
 
Please note that the tools availability depends on the project the plugin is applied to. For more details please refer to the
[supported tools](docs/supported-tools.md) page.

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
        classpath 'com.novoda:gradle-static-analysis-plugin:0.5.1'
    }
}

apply plugin: 'com.novoda.static-analysis'
```
        
or from the [Gradle Plugins Repository](https://plugins.gradle.org/):

```gradle
plugins {
    id 'com.novoda.static-analysis' version '0.5.1'
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
    detekt { }
    lintOptions { }
}
```

This will enable all the tools with their default settings. For more advanced configurations, please refer to the
[advanced usage](docs/advanced-usage.md) and to the [supported tools](docs/supported-tools.md) pages.

## Roadmap
The plugin is under active development and to be considered in **beta stage**. It is routinely used by many Novoda projects and
by other external projects with no known critical issues. The API is supposed to be relatively stable, but there still may be
breaking changes as we move towards version 1.0.

Future improvements can be found on the repository's
[issue tracker](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement).
