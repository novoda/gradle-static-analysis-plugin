# Findbugs
[Findbugs](http://findbugs.sourceforge.net/) is a static analysis tool that looks for potential bugs in Java code. It does not support Kotlin.
It can be used in both pure Java, and Android Java projects. It then only makes sense to have Findbugs enabled if you have Java code in your project.
The plugin only runs Findbugs on projects that contain the Java or the Android plugin.

## Table of contents
 * [Configure Findbugs](#configure-findbugs)
 * [Findbugs in mixed-language projects](#findbugs-in-mixed-language-projects)

---

## Configure Findbugs
Enabling and configuring Findbugs for a project is done through the `findbugs` closure:

```gradle
findbugs {
    toolVersion // A string, most likely '3.0.1' — the latest Findbugs release (for a long time)
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    excludeFilter // A file containing the Findbugs exclusions, e.g., teamPropsFile('static-analysis/findbugs-excludes.xml')
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

(assuming you're using the Novoda scaffolding system, see [Example configurations](#example-configurations) for more details)

You can have multiple `exclude` statements.

For more informations about Findbugs rules, refer to the [official website](http://findbugs.sourceforge.net/bugDescriptions.html).

## Findbugs in mixed-language projects
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


