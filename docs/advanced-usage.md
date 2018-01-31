# Advanced configuration

The plugin supports a number of advanced behaviours. For example, it can be used to set a baseline of warnings and errors below which
the build will not fail, which is very useful for legacy projects.

## Table of contents
 * [Configurable failure thresholds](#configurable-failure-thresholds)
 * [Improve the report with a base URL](#improve-the-report-with-a-base-URL)
 * [Add exclusions with `exclude` filters](#add-exclusions-with-exclude-filters)
 * [Add exclusions with Android build variants](#add-exclusions-with-Android-build-variants)

---

## Configurable failure thresholds
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

## Improve the report with a base URL
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

## Add exclusions with `exclude` filters
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

Please note that this is not supported for Detekt. To exclude files in Detekt, please refer to the specific tool documentation
in the [supported tools](supported-tools.md) page.

## Add exclusions with Android build variants
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

Please note that this is not supported for Detekt yet.