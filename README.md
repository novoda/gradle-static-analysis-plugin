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
- convenient way of sharing same setup across different projects (TBD),
- healthy, versionable and configurable defaults (TBD).

The plugin is **under early development** and to be considered in pre-alpha stage. At the moment not all integrations
have been completed. The table below summarises the current status.

Tool | Android | Java
:----:|:--------:|:--------:
`Checkstyle` | :white_check_mark: | :white_check_mark:
`PMD` | :white_check_mark: | :white_check_mark:
`FindBugs` | :white_check_mark: | :white_check_mark:
<br/>
