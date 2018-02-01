# Supported tools

The plugin supports several static analysis tools. The availability of each tool depends on the project the plugin is applied to.
Some tools only support Java code, some only Kotlin code, and some only work on Android projects. To be precise:

Tool | Java | Android<br/>(Java) | Kotlin | Android<br/>(Kotlin)
---- | -------- | -------- | ----- | -----
[`Checkstyle`](https://checkstyle.sourceforge.net) | :white_check_mark: | :white_check_mark: | — | —
[`PMD`](https://pmd.github.io) | :white_check_mark: | :white_check_mark: | — | —
[`FindBugs`](http://findbugs.sourceforge.net/) | :white_check_mark: | :white_check_mark: | — | —
[`Detekt`](https://github.com/arturbosch/detekt) | — | — | :white_check_mark: | :white_check_mark:
[`Android Lint`](https://developer.android.com/studio/write/lint.html) | — | :white_check_mark:️ | — | :white_check_mark:️
[`KtLint`\*](https://github.com/shyiko/ktlint) | — | — | ✖️ | ✖️

_\* Not supported [yet](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3A%22new+tool%22)_

For additional informations and tips on how to obtain advanced behaviours with the plugin and its tools, please refer to the
[advanced usage](advanced-usage.md) page.

## Table of contents
 * [Enable and disable tools](#enable-and-disable-tools)
 * Configure the tools
   * [Detekt](tools/detekt.md)
   * [Checkstyle](tools/checkstyle.md)
   * [PMD](tools/pmd.md)
   * [Findbugs](tools/findbugs.md)
   * [Android Lint](tools/android_lint.md)
   * KtLint — _COMING SOON_
 * [Example configurations](#example-configurations)

---

## Enable and disable tools
In order to enable a tool, you just need to add it to the `staticAnalysis` closure. To enable all supported tools with their default configurations:

```gradle
staticAnalysis {
    penalty {
        // ... (optional)
    }

    checkstyle {}
    pmd {}
    findbugs {}
    detekt {}
}
```

To disable a tool, simply omit its closure from `staticAnalysis`. This means that, for example, this will not run any tools:

```gradle
staticAnalysis {
    penalty {
        // ...
    }
}
```

## Example configurations
If you want, you can use the Novoda [`team-props` scaffolding system](https://github.com/novoda/novoda/tree/master/team-props) as a starting point for setting
up your project. The repository contains a good example of [configuration](https://github.com/novoda/novoda/blob/master/team-props/static-analysis.gradle) for
the plugin, and [rulesets](https://github.com/novoda/novoda/tree/master/team-props/static-analysis) for all supported tools.

