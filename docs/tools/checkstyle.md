# Checkstyle
[Checkstyle](http://checkstyle.sourceforge.net/) is a code style static analysis tool for Java. It is supported for both pure Java and Java Android projects,
but it does not support Kotlin nor Kotlin Android projects. It then only makes sense to have Checkstyle enabled if you have Java code in your project. The
plugin only runs Checkstyle on projects that contain the Java or the Android plugin.

## Table of contents
 * [Configure Checkstyle](#configure-checkstyle)
 * [Checkstyle in mixed-language projects](#checkstyle-in-mixed-language-projects)

---

## Configure Checkstyle
Enabling and configuring Checkstyle for a project is done through the `checkstyle` closure:

```gradle
checkstyle {
    toolVersion // A string, as per http://checkstyle.sourceforge.net/releasenotes.html, e.g., '8.8'
    exclude // A fileTree, such as project.fileTree('src/test/java') to exclude Java unit tests
    configFile // A file containing the Checkstyle config, e.g., teamPropsFile('static-analysis/checkstyle-modules.xml')
    includeVariants { variant -> ... } // A closure to determine which variants (for Android) to include
}
```

(assuming you're using the Novoda scaffolding system, see [Example configurations](#example-configurations) for more details)

You can have multiple `exclude` statements.

For more informations about Checkstyle rules, refer to the [official website](http://checkstyle.sourceforge.net/checks.html).

## Checkstyle in mixed-language projects
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