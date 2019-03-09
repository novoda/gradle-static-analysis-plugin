Change Log
==========

[Version 0.8.1](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.8.1)
--------------------------

- Removed use of `classesDir` because deprecated in Gradle 5.x ([PR#174](https://github.com/novoda/gradle-static-analysis-plugin/pull/174),
[PR#178](https://github.com/novoda/gradle-static-analysis-plugin/pull/178),
[PR#179](https://github.com/novoda/gradle-static-analysis-plugin/pull/179),
[PR#180](https://github.com/novoda/gradle-static-analysis-plugin/pull/180))
- Included new versions of Ktlint in functional tests ([PR#167](https://github.com/novoda/gradle-static-analysis-plugin/pull/167))
- Added automatic tagging of snapshot releases ([PR#176](https://github.com/novoda/gradle-static-analysis-plugin/pull/176))


[Version 0.8](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.8)
--------------------------

- Fix integration for all versions of Ktlint plugin [PR#153](https://github.com/novoda/gradle-static-analysis-plugin/pull/153)
- Make Findbugs Html report generation optional [PR#154](https://github.com/novoda/gradle-static-analysis-plugin/pull/154)
```
staticAnalysis {
    findbugs {
        htmlReportEnabled false
    }
}
```
- Display total number of errors and warnings [PR#159](https://github.com/novoda/gradle-static-analysis-plugin/pull/159)
- Less verbose Findbugs output [PR#160](https://github.com/novoda/gradle-static-analysis-plugin/pull/160)

[Version 0.7](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.7)
--------------------------

- Fix Detekt RC9.x and RC10 integrations [PR#144](https://github.com/novoda/gradle-static-analysis-plugin/pull/144)
- Fix Ktlint integration [PR#145](https://github.com/novoda/gradle-static-analysis-plugin/pull/145)
  - 6.1.0 and 6.2.1 versions are broken for Android projects because of [a bug in Ktlint](https://github.com/JLLeitschuh/ktlint-gradle/issues/153#issuecomment-437176852)
- Make Findbugs Html report generation cacheable [PR#148](https://github.com/novoda/gradle-static-analysis-plugin/pull/148)
- Use Gradle composite builds in sample projects [PR#142](https://github.com/novoda/gradle-static-analysis-plugin/pull/142)
- Improve docs [PR#128](https://github.com/novoda/gradle-static-analysis-plugin/pull/128), [PR#132](https://github.com/novoda/gradle-static-analysis-plugin/pull/132)

[Version 0.6](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.6)
--------------------------

- Fix release to plugin portal ([PR#82](https://github.com/novoda/gradle-static-analysis-plugin/pull/82), [PR#83](https://github.com/novoda/gradle-static-analysis-plugin/pull/83))
- Improve plugin documentation and samples ([PR#85](https://github.com/novoda/gradle-static-analysis-plugin/pull/85),
[PR#97](https://github.com/novoda/gradle-static-analysis-plugin/pull/97),
[PR#99](https://github.com/novoda/gradle-static-analysis-plugin/pull/99),
[PR#100](https://github.com/novoda/gradle-static-analysis-plugin/pull/100),
[PR#101](https://github.com/novoda/gradle-static-analysis-plugin/pull/101),
[PR#113](https://github.com/novoda/gradle-static-analysis-plugin/pull/113),
[PR#123](https://github.com/novoda/gradle-static-analysis-plugin/pull/123), 
[PR#124](https://github.com/novoda/gradle-static-analysis-plugin/pull/124))
- Improve support for Android Lint ([PR#105](https://github.com/novoda/gradle-static-analysis-plugin/pull/105))
- Improve support for Detekt ([PR#90](https://github.com/novoda/gradle-static-analysis-plugin/pull/90), [PR#121](https://github.com/novoda/gradle-static-analysis-plugin/pull/121))
- Rename built-in `failOnWarnings` penalty to `failFast` ([PR#92](https://github.com/novoda/gradle-static-analysis-plugin/pull/92))
- Support multiple configurations for `Pmd`, `Findbugs`, `Checkstyle` ([PR#93](https://github.com/novoda/gradle-static-analysis-plugin/pull/93))
- Support automatic snapshot builds from `develop` ([PR#106](https://github.com/novoda/gradle-static-analysis-plugin/pull/106),[PR#107](https://github.com/novoda/gradle-static-analysis-plugin/pull/107))
- Automatically exclude Kotlin files from Java code quality tools ([PR#109](https://github.com/novoda/gradle-static-analysis-plugin/pull/109))
- Integrate [KtLint](https://github.com/shyiko/ktlint) ([PR#110](https://github.com/novoda/gradle-static-analysis-plugin/pull/110))

[Version 0.5.2](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.5.2)
--------------------------

- Add support for variants Filtering in Android Lint ([PR#79](https://github.com/novoda/gradle-static-analysis-plugin/pull/79))

[Version 0.5.1](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.5.1)
--------------------------

- Add `Violations` to public API ([PR#69](https://github.com/novoda/gradle-static-analysis-plugin/pull/69))
- Custom violations evaluators ([PR#68](https://github.com/novoda/gradle-static-analysis-plugin/pull/68))

[Version 0.5](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.5)
--------------------------

- Integrate [detekt](https://github.com/arturbosch/detekt), a static analysis tool for Kotlin ([PR#58](https://github.com/novoda/gradle-static-analysis-plugin/pull/58))
- Integrate [Android Lint](https://developer.android.com/studio/write/lint.html), a linter and static analysis tool for Android projects ([PR#62](https://github.com/novoda/gradle-static-analysis-plugin/pull/62))

[Version 0.4.1](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.4.1)
--------------------------

- Ensure invariant over multiple successive runs ([PR#30](https://github.com/novoda/gradle-static-analysis-plugin/pull/30))

[Version 0.4](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.4)
--------------------------

- Filtering for Android variants ([PR#28](https://github.com/novoda/gradle-static-analysis-plugin/pull/28))
- Support for rules as maven artifact ([PR#27](https://github.com/novoda/gradle-static-analysis-plugin/pull/27))
- Added support for custom base url for reports in logs ([PR#25](https://github.com/novoda/gradle-static-analysis-plugin/pull/25))

[Version 0.3.2](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.3.2)
--------------------------

- Better classes filtering for Findbugs tasks ([PR#23](https://github.com/novoda/gradle-static-analysis-plugin/pull/23))

[Version 0.3.1](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.3.1)
--------------------------

- Honor exclude filters in Findbugs tasks ([PR#20](https://github.com/novoda/gradle-static-analysis-plugin/pull/20))

[Version 0.3](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.3)
--------------------------

- Honour project variants when creating tasks for Checkstyle and PMD ([PR#16](https://github.com/novoda/gradle-static-analysis-plugin/pull/16))

[Version 0.2](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.2)
--------------------------

- Improved `exclude` rules support ([PR#8](https://github.com/novoda/gradle-static-analysis-plugin/pull/8))
- Enforced default effort and report level for Findbugs ([PR#6](https://github.com/novoda/gradle-static-analysis-plugin/pull/6))

[Version 0.1](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.1)
--------------------------

- Initial release.
