# gradle-static-analysis-plugin
**TL;DR:** A Gradle plugin to easily apply the same setup of static analysis tools across different Android or Java projects.<br/>
<br/>

### Why
Gradle supports many popular static analysis (Checkstyle, PMD, FindBugs) via a set of built-in
plugins. Using these plugins in an Android module will require an additional setup to compensate for the differences between
the model adopted by the Android plugin compared to the the Java one.<br/>

The `static-analysis-plugin` aims to provide:
- flexible, configurable penalty strategy for builds,
- easy, Android-friendly integration for all static analysis,
- convenient way of sharing same setup across different projects,
- healthy, versionable and configurable defaults.

### Current status

The plugin is **under early development** and to be considered in pre-alpha stage.

#### Static analysis tools supported

Tool | Android | Java
:----:|:--------:|:--------:
`Checkstyle` | :white_check_mark: | :white_check_mark:
`PMD` | :white_check_mark: | :white_check_mark:
`FindBugs` | :white_check_mark: | :white_check_mark:

#### Support for sharable configurations

TBD
