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
   * [Configure Checkstyle](#configure-checkstyle)
   * [Checkstyle in mixed-language projects](#checkstyle-in-mixed-language-projects)
 * [PMD](#pmd)
   * [Configure PMD](#configure-pmd)
   * [PMD in mixed-language projects](#pmd-in-mixed-language-projects)
 * [Findbugs](#findbugs)
   * [Configure Findbugs](#configure-findbugs)
   * [Findbugs in mixed-language projects](#findbugs-in-mixed-language-projects)
 * [Detekt](#detekt)
   * [IMPORTANT: setup Detekt](#important-setup-detekt)
   * [Configure Detekt](#configure-detekt)
   * [Exclude files from Detekt analysis](#exclude-files-from-detekt-analysis)
   * [Detekt in mixed-language projects](#detekt-in-mixed-language-projects)
 * KtLint — _COMING SOON_
 * Android Lint — _COMING SOON_
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

### Configure Checkstyle
Enabling and configuring Checkstyle for a project is done through the `checkstyle` closure:

```gradle
checkstyle {
    toolVersion // A string, as per http://checkstyle.sourceforge.net/releasenotes.html, e.g., '8.8'
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    configFile // A file containing the Checkstyle config, e.g., teamPropsFile('static-analysis/checkstyle-modules.xml')
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

You can have multiple `exclude` statements.

For more informations about Checkstyle rules, refer to the [official website](http://checkstyle.sourceforge.net/checks.html).

### Checkstyle in mixed-language projects
If your project mixes Java and Kotlin code, you most likely want to have an exclusion in place for all `*.kt` files. You can use the `exclude`
in the configuration closure, or you can do so by adding a suppressions file:

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

### Configure PMD
Enabling and configuring PMD for a project is done through the `pmd` closure:

```gradle
pmd {
    toolVersion // A string, as per https://pmd.github.io/pmd-6.0.1/pmd_release_notes.html, e.g., '6.0.1'
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    ruleSetFiles // A set of files containing PMD rulesets, e.g., rootProject.files('team-props/static-analysis/pmd-rules.xml')
    ruleSets = []   // Note: this is a workaround to make the <exclude-pattern>s in pmd-rules.xml actually work
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

You can have multiple `exclude` statements.

For more informations about PMD Java rules, refer to the [official website](https://pmd.github.io/pmd-6.0.1/pmd_rules_java.html).

### PMD in mixed-language projects
If your project mixes Java and Kotlin code, you most likely want to have an exclusion in place for all `*.kt` files. You can use the `exclude`
in the configuration closure, or you can do so by adding a suppressions file:

`pmd-rules.xml`
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<ruleset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  name="Novoda PMD rules"
  xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
  xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 http://pmd.sourceforge.net/ruleset_2_0_0.xsd">

  ...

  <!--region File exclusions-->
  <exclude-pattern>.*\.kt</exclude-pattern>
  <!--endregion-->

  ...

</ruleset>
```

## Findbugs
[Findbugs](http://findbugs.sourceforge.net/) is a static analysis tool that looks for potential bugs in Java code. It does not support Kotlin.
It can be used in both pure Java, and Android Java projects. It then only makes sense to have Findbugs enabled if you have Java code in your project.
The plugin only runs Findbugs on projects that contain the Java or the Android plugin.

### Configure Findbugs
Enabling and configuring Findbugs for a project is done through the `findbugs` closure:

```gradle
findbugs {
    toolVersion // A string, most likely '3.0.1' — the latest Findbugs release (for a long time)
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    excludeFilter // A file containing the Findbugs exclusions, e.g., teamPropsFile('static-analysis/findbugs-excludes.xml')
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

You can have multiple `exclude` statements.

For more informations about Findbugs rules, refer to the [official website](http://findbugs.sourceforge.net/bugDescriptions.html).

### Findbugs in mixed-language projects
If your project mixes Java and Kotlin code, you most likely want to have an exclusion in place for all `*.kt` files. You can use the `exclude`
in the configuration closure, or you can do so by adding a suppression to the filter file:

`findbugs-excludes.xml`
```xml
<FindBugsFilter>

  ...

  <Match>
    <Source name="~.*\.kt" />
  </Match>

  ...

</FindBugsFilter>
```

## Detekt
[Detekt](https://github.com/arturbosch/detekt) is a static analysis tool that looks for potential bugs and style violations in Kotlin code. It
does not support Java. It can be used in both pure Kotlin, and Android Kotlin projects. It then only makes sense to have Detekt enabled if you
have Kotlin code in your project. The plugin only runs Detekt on projects that contain the Kotlin or the Kotlin-Android plugin.

### IMPORTANT: setup Detekt
Unlike the other tools, the plugin **won't automatically add Detekt** to your project. If you forget to do it, the plugin will fail the build
with an error.

In order to use Detekt, you need to manually add it to **all** your Kotlin projects. You can refer to the
[official documentation](https://github.com/arturbosch/detekt/#gradlegroovy) for further details. Note that you should _not_ add the `detekt`
closure to your `build.gradle`s, unlike what the official documentation says. The `detekt` closure in the `staticAnalysis` configuration gets
applied to all Kotlin modules automatically.

In most common cases, adding Detekt to a project boils down to three simple steps:

 1. Add this statement to your root `build.gradle` project (change the version according to your needs):
    ```gradle
    plugins {
        id 'io.gitlab.arturbosch.detekt' version '1.0.0.RC6-2'
        // ...
    }
    ```
 2. Add this statement to each Kotlin project's `build.gradle`s:
    ```gradle
    plugins {
        id 'io.gitlab.arturbosch.detekt'
        // ...
    }
    ```

### Configure Detekt
Enabling and configuring Detekt for a project is done through the `detekt` closure. The closure behaves exactly like the
[standard Detekt plugin](https://github.com/arturbosch/detekt#using-the-detekt-gradle-plugin) does in Gradle, which is to say, quite differently
from how the other tools' configurations closures work. For example:

```gradle
detekt {
    profile('main') {
        input = // A string pointing to a project's sources. E.g., "$projectDir/src/main/java"
        config = // A file containing the Detekt configuration, e.g., teamPropsFile('static-analysis/detekt-config.yml')
        filters = // A comma-separated list of regex exclusions, e.g., '.*test.*,.*/resources/.*,.*/tmp/.*'
        output = // A string pointing to the output directory for the reports, e.g., "$projectDir/build/reports/detekt"
    }
}
```

You need to provide **at a minimum** the `config` and `output` values. It's important that you do _not_ specify a `warningThreshold` nor a `failThreshold`
in the Detekt configuration file as it will interfere with the functioning of the Static Analysis plugin's threshold counting. For the same reason, make
sure that `failFast` is set to `false` in the Detekt configuration.

For more informations about Detekt rules, refer to the [official website](https://github.com/arturbosch/detekt/tree/master/detekt-generator/documentation).

### Exclude files from Detekt analysis

In order to exclude files from Detekt analysis, you have to use the facilities provided by the Detekt plugin in the `detekt` configuration closure. This means,
you have to provide a value to the `filters` property that contains the exclusion pattern(s) you wish Detekt to ignore.

The `filters` property expects a string consisting in a comma-separated list of regular expression patterns, e.g., `'.*test.*,.*/resources/.*,.*/tmp/.*'`
(which would exclude any path containing the word `test`, or any path containing a directory called `resources` or `tmp`).

### Detekt in mixed-language projects
If your project mixes Java and Kotlin code, you don't need to have an exclusion in place for all `*.java` files. Detekt itself only looks for
`*.kt` files, so no further configuration is required.

## Example configurations
If you want, you can use the Novoda [`team-props` scaffolding system](https://github.com/novoda/novoda/tree/master/team-props) as a starting point for setting
up your project. The repository contains a good example of [configuration](https://github.com/novoda/novoda/blob/master/team-props/static-analysis.gradle) for
the plugin, and [rulesets](https://github.com/novoda/novoda/tree/master/team-props/static-analysis) for all supported tools.

