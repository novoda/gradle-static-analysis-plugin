# PMD
[PMD](https://pmd.github.io/) is an extensible cross-language static code analyser. It supports Java and [several other languages](https://pmd.github.io/#about),
but not Kotlin. It can be used in both pure Java, and Android Java projects. It then only makes sense to have PMD enabled if you have Java code in your project.
The plugin only runs PMD on projects that contain the Java or the Android plugin.

## Table of contents
 * [Configure PMD](#configure-pmd)
 * [PMD in mixed-language projects](#pmd-in-mixed-language-projects)

---

## Configure PMD
Enabling and configuring PMD for a project is done through the `pmd` closure:

```gradle
pmd {
    toolVersion // A string, as per https://pmd.github.io/pmd-6.0.1/pmd_release_notes.html, e.g., '6.0.1'
    incrementalAnalysis = true // Available as of Gradle 5.6
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    ruleSetFiles // A set of files containing PMD rulesets, e.g., rootProject.files('team-props/static-analysis/pmd-rules.xml')
    ruleSets = []   // Note: this is a workaround to make the <exclude-pattern>s in pmd-rules.xml actually work
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

(assuming you're using the Novoda scaffolding system, see [Example configurations](#example-configurations) for more details)

You can have multiple `exclude` statements.

For more informations about PMD Java rules, refer to the [official website](https://pmd.github.io/pmd-6.0.1/pmd_rules_java.html).

## PMD in mixed-language projects
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
