# Custom violations evaluator (incubating)

> Since: [`0.5.1`](https://github.com/novoda/gradle-static-analysis-plugin/releases/tag/v0.5.1)
>
> :warning: This is an **experimental**, incubating feature that may be subject to breaking API changes at any time!

The plugin uses a [`ViolationsEvaluator`][violationsevaluatorcode] to determine what to do with the results collected from all the active
tools (if any). The built-in behaviour is provided by the [`DefaultViolationsEvaluator`][defaultviolationsevaluatorcode], which you can
read more about [below](#the-defaultviolationsevaluator). The plugin's violations evaluation behaviour is not fixed, and it can be
customised by providing an implementation of the [`ViolationsEvaluator`][violationsevaluatorcode] interface.

## Table of contents
 * [The `DefaultViolationsEvaluator`](#the-defaultviolationsevaluator)
 * [Creating a custom violations evaluator](#creating-a-custom-violations-evaluator)

---

## The `DefaultViolationsEvaluator`
The plugin has a default mechanism to decide whether to consider a build as passing or failed. The mechanism is manifesting itself
as the `penalty` closure:

```gradle
staticAnalysis {
    penalty {
        maxErrors = 0
        maxWarnings = 10
    }
    //...
}
```

This closure instructs the plugin to use a [`DefaultViolationsEvaluator`][defaultviolationsevaluatorcode] that will count the number of
errors and warnings and compare them against the set thresholds. For more details, see the
[Configurable failure thresholds](../advanced-usage.md#configurable-failure-thresholds) documentation.

## Creating a custom violations evaluator
In order to provide a custom evaluator, you can implement the [`ViolationsEvaluator`][violationsevaluatorcode] interface and provide
that implementation to the `evaluator` property of the `staticAnalysis` closure. The [`ViolationsEvaluator`][violationsevaluatorcode]
can be provided as a closure as well:

```gradle
staticAnalysis {
	evaluator {	Set<Violations> allViolations ->
       // add your evaluation logic here
	}
	//...
}
```

The `evaluator` is invoked after all the `collectViolations` tasks have been completed, and is the last step in executing the plugin's
main task, `evaluateViolations`.

The evaluation logic can be any arbitrary function that respects this contract:
 * The evaluator receives a set containing all the [`Violations`][violationscode] that have been collected by the tools (one per tool)
 * If the build is to be considered successful, then the evaluator will run to completion without throwing exceptions
 * If the build is to be considered failed, then the evaluator will throw a `GradleException`

Anything that respect such contract is valid. For example, a custom evaluator might:
 * Collect all the report files and upload them somewhere, or send them to Slack or an email address
 * Use the GitHub API to report the issues on the PR that the build is running on, à la [GNAG](https://github.com/btkelly/gnag)
 * Only break the build if there are errors or warnings in one specific report
 * Or anything else that you can think of

Please note that the presence of an `evaluator` property will make the plugin ignore the `penalty` closure and its thresholds. If you
want to provide behaviour on top of the default [`DefaultViolationsEvaluator`][defaultviolationsevaluatorcode], you can have your own
evaluator run its logic and then delegate the thresholds counting to an instance of `DefaultViolationsEvaluator` you create.

[violationsevaluatorcode]: https://github.com/novoda/gradle-static-analysis-plugin/blob/master/plugin/src/main/groovy/com/novoda/staticanalysis/ViolationsEvaluator.groovy
[defaultviolationsevaluatorcode]: https://github.com/novoda/gradle-static-analysis-plugin/blob/master/plugin/src/main/groovy/com/novoda/staticanalysis/DefaultViolationsEvaluator.groovy
[violationscode]: https://github.com/novoda/gradle-static-analysis-plugin/blob/master/plugin/src/main/groovy/com/novoda/staticanalysis/internal/Violations.groovy
