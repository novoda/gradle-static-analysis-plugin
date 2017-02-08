# gradle-static-analysis-plugin
[![](https://ci.novoda.com/buildStatus/icon?job=gradle-static-analysis-plugin)](https://ci.novoda.com/job/gradle-static-analysis-plugin/lastSuccessfulBuild/console) [![](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE.txt) [![Bintray](https://api.bintray.com/packages/novoda/maven/gradle-static-analysis-plugin/images/download.svg) ](https://bintray.com/novoda/maven/gradle-static-analysis-plugin/_latestVersion)

A Gradle plugin to easily apply the same setup of static analysis tools across different Android or Java projects.<br/>

## Description

Gradle supports many popular static analysis (Checkstyle, PMD, FindBugs, etc) via a set of built-in
plugins. Using these plugins in an Android module will require an additional setup to compensate for the differences between
the model adopted by the Android plugin compared to the the Java one.<br/>

The `gradle-static-analysis-plugin` aims to provide:
- flexible, configurable penalty strategy for builds,
- easy, Android-friendly integration for all static analysis,
- convenient way of sharing same setup across different projects,
- healthy, versionable and configurable defaults.

## Adding to your project

The plugin is released in jcenter and can be included as a classpath dependency:
```gradle
buildscript {
    repositories {
       jcenter()
    }
    dependencies {
        classpath 'com.novoda:gradle-static-analysis-plugin:0.3.1'
    }
}
```
and then apply the plugin via:
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
    findbugs {}
}
```

#### Configurable thresholds
Users can define maximum amount of warnings and errors tolerated in a build via the gradle configuration:
```gradle
staticAnalysis {
    penalty {
        maxErrors = 10
        maxWarnings = 10
    }
}
```
Violations are then collected while running all the static analysis tools enabled in the project and split between errors and warnings.
Only in the end they are cumulatively evaluated against the thresholds provided in the configuration to decide whether the build should fail or not.

#### Better output
Build logs will show an overall report of how many violations have been found during the analysis and the links to
the relevant html reports, for instance:
```
    > PMD rule violations were found (2 errors, 2 warnings). See the reports at:
    - file:///foo/project/build/reports/pmd/main.html
    - file:///foo/project/build/reports/pmd/main2.html
    - file:///foo/project/build/reports/pmd/main3.html
    - file:///foo/project/build/reports/pmd/main4.html
```

#### Out-of-the-box support for Android projects
Android projects use a gradle model that is not compatible with the Java one, supported by the built-in static analysis tools plugins.
Applying `gradle-static-analysis-plugin` to your Android project will make sure all the necessary tasks are created and correctly configured
without any additional hassle.

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

### Current status / Roadmap

The plugin is **under early development** and to be considered in pre-alpha stage.

#### Static analysis tools supported

Tool | Android | Java | Documentation |
:----:|:--------:|:--------:|:----:|
`Checkstyle` | :white_check_mark: | :white_check_mark: | _Coming Soon_ |
`PMD` | :white_check_mark: | :white_check_mark: | _Coming Soon_ |
`FindBugs` | :white_check_mark: | :white_check_mark: | _Coming Soon_ |

#### Support for sharable configurations

We plan to add support for consuming rules (eg: configuration files for Checkstyle or PMD, default exclude filters, etc) via a
separate artifact you can share across projects. _More info to come._
