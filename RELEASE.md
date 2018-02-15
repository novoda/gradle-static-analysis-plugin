# Releasing

1. Bump version code in `publish.gradle`
1. Update examples in `README.md` to use the latest version
1. Create an entry in `CHANGELOG.md` containing the changes in that release. This entry needs to follow a certain pattern which can be found in `publish.gradle`.
1. Create a pull request to `develop` containing the above mentioned changes similar to [this](https://github.com/novoda/gradle-static-analysis-plugin/pull/81) one
1. Merge `develop` into `master`
1. Execute the [build job](https://ci.novoda.com/job/gradle-static-analysis-plugin/) manually with `BINTRAY_DRY_RUN=false`
1. After the release is successful do a manual [github release](https://github.com/novoda/gradle-static-analysis-plugin/releases) with the newly created tag 

This releases the plugin to [bintray](https://bintray.com/novoda/maven/gradle-static-analysis-plugin) and the [Gradle Plugins Repository](https://plugins.gradle.org/plugin/com.novoda.static-analysis).
