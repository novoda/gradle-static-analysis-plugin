package com.novoda.staticanalysis.internal.spotbugs

import com.novoda.staticanalysis.StaticAnalysisExtension
import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.Configurator
import com.novoda.staticanalysis.internal.VariantFilter
import com.novoda.staticanalysis.internal.findbugs.CollectFindbugsViolationsTask
import com.novoda.staticanalysis.internal.findbugs.GenerateFindBugsHtmlReport
import org.gradle.api.*
import org.gradle.api.tasks.SourceTask

import static com.novoda.staticanalysis.internal.Exceptions.handleException
import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class SpotBugsConfigurator implements Configurator {

    private static final String SPOTBUGS_PLUGIN = 'com.github.spotbugs'
    private static final String SPOTBUGS_NOT_APPLIED = "The SpotBugs plugin is configured but not applied. Please apply the plugin: $SPOTBUGS_PLUGIN in your build script."
    private static final String SPOTBUGS_CONFIGURATION_ERROR = "A problem occurred while configuring SpotBugs."

    private final Project project
    private final Violations violations
    private final Task evaluateViolations
    private final VariantFilter variantFilter
    protected boolean htmlReportEnabled = true
    protected boolean configured = false

    static SpotBugsConfigurator create(Project project,
                                       NamedDomainObjectContainer<Violations> violationsContainer,
                                       Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('SpotBugs')
        return new SpotBugsConfigurator(project, violations, evaluateViolations)
    }

    SpotBugsConfigurator(Project project, Violations violations, Task evaluateViolations) {
        this.project = project
        this.violations = violations
        this.evaluateViolations = evaluateViolations
        this.variantFilter = new VariantFilter(project)
    }

    @Override
    void execute() {
        project.extensions.findByType(StaticAnalysisExtension).ext.spotbugs = { Closure config ->
            if (!project.plugins.hasPlugin(SPOTBUGS_PLUGIN)) {
                throw new GradleException(SPOTBUGS_NOT_APPLIED)
            }

            configureSpotBugsExtension(config)

            project.plugins.withId('com.android.application') {
                configureAndroidWithVariants(variantFilter.filteredApplicationVariants)
            }
            project.plugins.withId('com.android.library') {
                configureAndroidWithVariants(variantFilter.filteredLibraryVariants)
            }
            project.plugins.withId('java') {
                configureJavaProject()
            }
        }
    }

    private void configureSpotBugsExtension(Closure config) {
        try {
            def spotbugs = project.spotbugs
            spotbugs.ext.includeVariants = { Closure<Boolean> filter ->
                variantFilter.includeVariantsFilter = filter
            }
            spotbugs.ext.htmlReportEnabled = { boolean enabled -> this.htmlReportEnabled = enabled }
            config.delegate = spotbugs
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        } catch (Exception exception) {
            handleException(SPOTBUGS_CONFIGURATION_ERROR, exception)
        }
    }

    protected void configureAndroidWithVariants(DomainObjectSet variants) {
        if (configured) return

        variants.all { configureVariant(it) }
        variantFilter.filteredTestVariants.all { configureVariant(it) }
        variantFilter.filteredUnitTestVariants.all { configureVariant(it) }
        configured = true
    }

    private void configureVariant(variant) {
        createToolTaskForAndroid(variant)
        def collectViolations = createCollectViolations(getToolTaskNameFor(variant), violations)
        evaluateViolations.dependsOn collectViolations
    }

    private void createToolTaskForAndroid(variant) {
        createTask(project, getToolTaskNameFor(variant), Class.forName('com.github.spotbugs.SpotBugsTask')) { SourceTask task ->
            def javaCompile = javaCompile(variant)
            def androidSourceDirs = variant.sourceSets.collect {
                it.javaDirectories
            }.flatten()
            task.description = "Run SpotBugs analysis for ${variant.name} classes"
            task.setSource(androidSourceDirs)
            task.classpath = javaCompile.classpath
            task.extraArgs '-auxclasspath', androidJar
            task.conventionMapping.map("classes") {
                project.fileTree(javaCompile.destinationDir)
            }
            task.dependsOn javaCompile
        }
    }

    private void configureJavaProject() {
        if (configured) return

        project.sourceSets.all { sourceSet ->
            def collectViolations = createCollectViolations(getToolTaskNameFor(sourceSet), violations)
            evaluateViolations.dependsOn collectViolations
        }
        configured = true
    }

    private def createCollectViolations(String taskName, Violations violations) {
        if (htmlReportEnabled) {
            createHtmlReportTask(taskName)
        }
        createTask(project, "collect${taskName.capitalize()}Violations", CollectFindbugsViolationsTask) { task ->
            def spotbugs = project.tasks[taskName] as SourceTask
            configureToolTask(spotbugs)
            task.xmlReportFile = spotbugs.reports.xml.destination
            task.violations = violations

            if (htmlReportEnabled) {
                task.dependsOn project.tasks["generate${taskName.capitalize()}HtmlReport"]
            } else {
                task.dependsOn spotbugs
            }
        }
    }

    private void createHtmlReportTask(String taskName) {
        createTask(project, "generate${taskName.capitalize()}HtmlReport", GenerateFindBugsHtmlReport) { GenerateFindBugsHtmlReport task ->
            def spotbugs = project.tasks[taskName]
            task.xmlReportFile = spotbugs.reports.xml.destination
            task.htmlReportFile = new File(task.xmlReportFile.absolutePath - '.xml' + '.html')
            task.classpath = spotbugs.spotbugsClasspath
            task.dependsOn spotbugs
        }
    }

    private static void configureToolTask(SourceTask task) {
        task.group = 'verification'
        task.exclude '**/*.kt'
        task.ignoreFailures = true
        task.reports.xml.enabled = true
        task.reports.html.enabled = false
    }

    private static String getToolTaskNameFor(named) {
        "spotbugs${named.name.capitalize()}"
    }

    private static def javaCompile(variant) {
        if (variant.hasProperty('javaCompileProvider')) {
            variant.javaCompileProvider.get()
        } else {
            variant.javaCompile
        }
    }

    private def getAndroidJar() {
        "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
    }
}
