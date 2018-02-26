# Advanced configuration

The plugin supports a number of advanced behaviours. For example, it can be used to set a baseline of warnings and errors below which
the build will not fail, which is very useful for legacy projects.

## Table of contents
 * [Configurable failure thresholds](#configurable-failure-thresholds)
 * [Improve the report with a base URL](#improve-the-report-with-a-base-URL)
 * [Add exclusions with `exclude` filters](#add-exclusions-with-exclude-filters)
 * [Add exclusions with Android build variants](#add-exclusions-with-android-build-variants)
 * [Consume rules from an artifact](#consume-rules-from-an-artifact)
 * [Custom violations evaluator (**incubating**)](incubating/custom-evaluator.md#custom-violations-evaluator-incubating)

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

If you don't specify a `penalty` configuration, the plugin will use the [default threshold values][penaltyextensioncode], which are to
allow any warning, but break the build on any error.

### Default `penalty` profiles
Besides manually specifying thresholds, the plugin includes a few built-in `penalty` profiles that can be used as follows:

* `none`
    ```gradle
    staticAnalysis {
        penalty none
    }
    ```
    In this case the build won't fail no matter how many violations (warnings or errors) are found.

* `failOnErrors` (default policy)
    ```gradle
    staticAnalysis {
        penalty failOnErrors
    }
    ```
    This will break the build if any error is found. Warnings instead are only logged and will not break the build.

* `failFast`
    ```gradle
    staticAnalysis {
        penalty failFast
    }
    ```
    This policy will fail the build if any warning or error is found. It is a zero-tolerance policy, useful to keep
    the codebase clean from any warnings or errors over time.

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
in the [Detekt](tools/detekt.md#exclude-files-from-detekt-analysis) page.

## Add exclusions with Android build variants
Sometimes using `exclude` filters could be not enough. When using the plugin in an Android project you may want to consider
only one specific variant as part of the analysis. The plugin provides a way of defining which Android variants should be included
via the `includeVariants` method added to each tool extension. E.g.,

```gradle
staticAnalysis {
    findbugs {
        includeVariants { variant ->
            variant.name == 'debug' // only the debug variant
        }
    }
}
```

Please note that this is not yet supported for Detekt.

[penaltyextensioncode]: https://github.com/novoda/gradle-static-analysis-plugin/blob/master/plugin/src/main/groovy/com/novoda/staticanalysis/PenaltyExtension.groovy


## Consume rules from an artifact     
In order to reuse your rules among multiple projects or to easily use an open source rule set, we added support for consuming the 
rules for all supported tools from a maven artifact. 


### Rules artifact
A rule artifact is just a bundle of files that is published on a maven repository as artifact. An example of how to do that can be be found [here](https://github.com/novoda/novoda/blob/master/scaffolding/build.gradle).
In this case we bundle the files as jar using the using the [java plugin](https://docs.gradle.org/current/userguide/java_plugin.html) and publish it using our [bintray-release plugin](https://github.com/novoda/bintray-release). 


### How to define a dependency from a rules artifact
In order to access the files inside a rule artifact you have to first define an entry in the `rules {}` extension of the plugin. An entry is defined by a name and a maven coordinate, as follows:
```
staticAnalysis {
    rules {
        novoda {
            maven 'com.novoda:static-analysis-rules:0.2'
        }
    }
}
```

### Access rules from artifact
Once you have defined a rule artifact you can access the files inside it specifying the path of the file inside the bundle, eg:
```
def modules = rules.novoda['checkstyle-modules.xml']
```
Note that `modules` is defined as [`TextResource`](https://docs.gradle.org/current/dsl/org.gradle.api.resources.TextResource.html), that is a read-only body of text backed by a string, file, archive entry, or other source. Some of the tools already accept `TextResource` as value for some of their configuration, while in other cases you have to transform a `TextResource` into a `File` or a path. Some examples of using a rule artifact in the supported tools is provided below:

Checkstyle
```gradle
checkstyle {
    toolVersion '8.8'
    config rules.novoda['checkstyle-modules.xml']
}
```

PMD
```
pmd {
    toolVersion '6.0.1'
    ruleSetFiles = project.files(rules.novoda['pmd-rules.xml'].asFile().path)
}
```

FindBugs
```
findbugs {
    excludeFilter rules.novoda['findbugs-excludes.xml'].asFile()
}
```

detekt
```
detekt {
    profile('main') {
        config = rules.novoda['detekt.yml'].asFile().path
    }
}
```

Android Lint
```
lintOptions {
    lintConfig = rules.novoda['lint-config.xml'].asFile()
}
```
