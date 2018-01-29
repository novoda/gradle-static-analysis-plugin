# Gradle static analysis plugin
[![](https://ci.novoda.com/buildStatus/icon?job=gradle-static-analysis-plugin)](https://ci.novoda.com/job/gradle-static-analysis-plugin/lastSuccessfulBuild) [![](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE.txt) [![Bintray](https://api.bintray.com/packages/novoda/maven/gradle-static-analysis-plugin/images/download.svg) ](https://bintray.com/novoda/maven/gradle-static-analysis-plugin/_latestVersion)

A Gradle plugin to easily apply the same setup of static analysis tools across different Android or Java projects.

## Description
Gradle supports many popular static analysis (Checkstyle, PMD, FindBugs, etc) via a set of built-in plugins.
Using these plugins in an Android module will require an additional setup to compensate for the differences between
the model adopted by the Android plugin compared to the the Java one.

The `gradle-static-analysis-plugin` aims to provide:
- flexible, configurable penalty strategy for builds
- easy, Android-friendly integration for all static analysis
- convenient way of sharing same setup across different projects
- healthy, versionable and configurable defaults

### Suported static analysis tools

Tool | Java | Android (Java) | Kotlin and<br/>Android (Kotlin)
---- | -------- | -------- | -----
[`Checkstyle`](https://checkstyle.sourceforge.net) | :white_check_mark: | :white_check_mark: | ✖️
[`PMD`](https://pmd.github.io) | :white_check_mark: | :white_check_mark: | ✖️
[`FindBugs`](http://findbugs.sourceforge.net/) | :white_check_mark: | :white_check_mark: | ✖️
[`Detekt`](https://github.com/arturbosch/detekt) | ✖️ | ✖️ | :white_check_mark: | ✖️
[`KtLint`\*](https://github.com/shyiko/ktlint) | ✖️ | ✖️ | ✖️
[`Android Lint`\*](https://developer.android.com/studio/write/lint.html) | ✖️ | ✖️ | ✖️

_\* Not supported [yet](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3A%22new+tool%22)_

### Out-of-the-box support for Android and Kotlin projects
Android projects use a Gradle model that is not compatible with the Java one, supported by the built-in static analysis tools plugins.
Applying `gradle-static-analysis-plugin` to your Android project will make sure all the necessary tasks are created and correctly configured
without any additional hassle.

## Add the plugin to your project
The plugin is available on jCenter and can be included as a classpath dependency:

```gradle
buildscript {
    repositories {
       jcenter()
    }
    dependencies {
        classpath 'com.novoda:gradle-static-analysis-plugin:0.4.1'
    }
}
```

You can then apply the plugin via:

```gradle
apply plugin: 'com.novoda.static-analysis'
```

## Simple usage
A typical configuration for the plugin will look like:

```gradle
staticAnalysis {
    penalty {
        maxErrors = 0
        maxWarnings = 100
    }
    checkstyle {
        configFile project.file('path/to/modules.xml')
    }
    pmd {
        ruleSetFiles = project.files('path/to/rules.xml')
    }
    findbugs {
        // ...
    }
}
```

#### Configurable thresholds
Users can define maximum amount of warnings and errors tolerated in a build via the Gradle configuration:

```gradle
staticAnalysis {
    penalty {
        maxErrors = 10
        maxWarnings = 10
    }
}
```

Violations are then collected while running all the static analysis tools enabled in the project and split between errors and warnings.
Only in the end they are cumulatively evaluated against the thresholds provided in the configuration to decide whether the build should
fail or not.

## Advanced configuration

### Improve the report with a base URL
Build logs will show an overall report of how many violations have been found during the analysis and the links to
the relevant HTML reports, for instance:

```
    > PMD rule violations were found (2 errors, 2 warnings). See the reports at:
    - file:///foo/project/build/reports/pmd/main.html
    - file:///foo/project/build/reports/pmd/main2.html
    - file:///foo/project/build/reports/pmd/main3.html
    - file:///foo/project/build/reports/pmd/main4.html
```

It's possible to specify a custom renderer for the report urls in the logs via the `logs` extension. This can be useful in CI
environments, where the local paths are not reachable directly. For instance the snippet below will replace the base URL with
one mapping to an hypothetical Jenkins workspace:

```gradle
staticAnalysis {
    ...
    logs {
        reportBaseUrl "http://ci.mycompany.com/job/myproject/ws/app/build/reports"
    }
}
```

This way, in the CI logs you will see the report URLs printed as:

```
> Checkstyle rule violations were found (0 errors, 1 warnings). See the reports at:
- http://ci.mycompany.com/job/myproject/ws/app/build/reports/checkstyle/main.html
```

And that will make them easier to follow them to the respective reports. More info on the topic can be found in the
[`LogsExtension`](blob/master/plugin/src/main/groovy/com/novoda/staticanalysis/LogsExtension.groovy)
Groovydocs.

#### Support for `exclude` filters
You can specify custom patterns to exclude specific files from the static analysis. All you have to do is to specify `exclude`
in the configuration of your tool of choice:

```gradle
staticAnalysis {
    findbugs {
        exclude '**/*Test.java' // file pattern
        exclude project.fileTree('src/test/java') // entire folder
        exclude project.file('src/main/java/foo/bar/Constants.java') // specific file
        exclude project.sourceSets.main.java.srcDirs // entire source set
    }
}
```

#### Support for Android variants
Sometimes using `exclude` filters could be not enough. When using the plugin in an Android project you may want to consider
only one specific variant as part of the analysis. The plugin provides a way of defining which Android variants should be included
via the `includeVariants` method added to each tool extension. E.g.,

```gradle
staticAnalysis {
    findbugs {
        includeVariants { variant ->
            variant.name.equals('debug') // only the debug variant
        }
    }
}
```

## Roadmap
The plugin is under active development and to be considered in **beta stage**. It is routinely used by many Novoda projects and
by other external projects with no known critical issues. The API is supposed to be relatively stable, but there still may be
breaking changes as we move towards version 1.0.

Future improvements can be found on the repository's
[issue tracker](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement).
