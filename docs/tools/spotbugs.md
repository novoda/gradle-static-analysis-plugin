# SpotBugs
[SpotBugs](https://spotbugs.github.io/) is a static analysis tool that looks for potential bugs in Java code. It does not support Kotlin.
It can be used in both pure Java, and Android Java projects. It then only makes sense to have SpotBugs enabled if you have Java code in your project.
The plugin only runs SpotBugs on projects that contain the Java or the Android plugin.

## Table of contents
 * [Configure SpotBugs](#configure-spotbugs)
 * [SpotBugs in mixed-language projects](#spotbugs-in-mixed-language-projects)

---

## Configure SpotBugs
Enabling and configuring SpotBugs for a project is done through the `spotbugs` closure:

```gradle
spotbugs {
    toolVersion // Optional string, the latest SpotBugs release (currently 4.0.0-beta4)
    excludeFilter // A file containing the SpotBugs exclusions, e.g., teamPropsFile('static-analysis/spotbugs-excludes.xml')
    htmlReportEnabled true // Control whether html report generation should be enabled. `true` by default.
    includeVariants { variant -> ... } // A closure to determine which variants (only for Android) to include
}
```

(assuming you're using the Novoda scaffolding system, see [Example configurations](#example-configurations) for more details)

For more information about SpotBugs rules, refer to the [official website](https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html).

## SpotBugs in mixed-language projects
If your project mixes Java and Kotlin code, you will need to exclude your Kotlin files by using `excludeFilter`
