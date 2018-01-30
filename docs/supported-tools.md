# Supported tools

The plugin supports several static analysis tools. The availability of each tool depends on the project the plugin is applied to.
Some tools only support Java code, some only Kotlin code, and some only work on Android projects. To be precise:

Tool | Java | Android<br/>(Java) | Kotlin | Android<br/>(Kotlin)
---- | -------- | -------- | ----- | -----
[`Checkstyle`](https://checkstyle.sourceforge.net) | :white_check_mark: | :white_check_mark: | — | —
[`PMD`](https://pmd.github.io) | :white_check_mark: | :white_check_mark: | — | —
[`FindBugs`](http://findbugs.sourceforge.net/) | :white_check_mark: | :white_check_mark: | — | —
[`Detekt`](https://github.com/arturbosch/detekt) | — | — | :white_check_mark: | :white_check_mark:
[`KtLint`\*](https://github.com/shyiko/ktlint) | — | — | ✖️ | ✖️
[`Android Lint`\*](https://developer.android.com/studio/write/lint.html) | — | ✖️ | — | ✖️

_\* Not supported [yet](https://github.com/novoda/gradle-static-analysis-plugin/issues?q=is%3Aopen+is%3Aissue+label%3A%22new+tool%22)_

For additional informations and tips on how to obtain advanced behaviours with the plugin and its tools, please refer to the
[advanced usage](advanced-usage.md) page.

## Table of contents
 * [Enable and disable tools](#enable-and-disable-tools)
 * [Checkstyle](#checkstyle)
   * [Checkstyle in mixed-language projects](#checkstyle-in-mixed-language-projects)
 * [PMD](#pmd) _TODO_
 * [Findbugs](#findbugs) _TODO_
 * [Detekt](#detekt) _TODO_
 * KtLint _COMING SOON_
 * Android Lint _COMING SOON_
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

## Checkstyle
[Checkstyle](http://checkstyle.sourceforge.net/) is a code style static analysis tool for Java. It is supported for both pure Java and Java Android projects,
but it does not support Kotlin nor Kotlin Android projects. It then only makes sense to have Checkstyle enabled if you have Java code in your project. The
plugin only runs Checkstyle on projects that contain the Java or the Android plugin.

Enabling and configuring Checkstyle for a project is done through the `checkstyle` closure:

```gradle
checkstyle {
    toolVersion // A string, as per http://checkstyle.sourceforge.net/releasenotes.html, e.g., '8.8'
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    configFile // A string pointing to a Checkstyle config file, e.g., 'config/checkstyle-modules.xml'
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

For more informations about Checkstyle rules, refer to the [official website](http://checkstyle.sourceforge.net/checks.html).

### Checkstyle in mixed-language projects
If your project mixes Java and Kotlin code, you most likely want to have an exclusion in place for all `*.kt` files. You can do so by adding a suppressions file:

`checkstyle-suppressions.xml`
```xml
<?xml version="1.0"?>

<!DOCTYPE suppressions PUBLIC
  "-//Puppy Crawl//DTD Suppressions 1.1//EN"
  "http://www.puppycrawl.com/dtds/suppressions_1_1.dtd">

<suppressions>

  <!-- Exclude all Kotlin files -->
  <suppress checks=".*" files=".*\.kt" />

</suppressions>
```

You then need to reference this file from the Checkstyle configuration file:

`checkstyle-modules.xml`
```xml
<!DOCTYPE module PUBLIC
  "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
  "http://www.puppycrawl.com/dtds/configuration_1_3.dtd">

<module name="Checker">
    ...
    <module name="SuppressionFilter">
        <property name="file" value="team-props/static-analysis/checkstyle-suppressions.xml" />
    </module>
    ...
</module>
```

(assuming you're using the Novoda scaffolding system, see [Example configurations](#example-configurations) for more details)

## PMD
[PMD](https://pmd.github.io/) is an extensible cross-language static code analyser. It supports Java and [several other languages](https://pmd.github.io/#about),
but not Kotlin. It can be used in both pure Java, and Android Java projects. It then only makes sense to have PMD enabled if you have Java code in your project.
The plugin only runs PMD on projects that contain the Java or the Android plugin.

## Example configurations
If you want, you can use the Novoda [`team-props` scaffolding system](https://github.com/novoda/novoda/tree/master/team-props) as a starting point for setting
up your project. The repository contains a good example of [configuration](https://github.com/novoda/novoda/blob/master/team-props/static-analysis.gradle) for
the plugin, and [rulesets](https://github.com/novoda/novoda/tree/master/team-props/static-analysis) for all supported tools.

