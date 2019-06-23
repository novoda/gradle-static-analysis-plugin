package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.CollectViolationsTask
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.tasks.SourceSet

import java.nio.file.Path

import static com.novoda.staticanalysis.internal.TasksCompat.configureNamed
import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class FindbugsConfigurator extends CodeQualityConfigurator<FindBugs, FindBugsExtension> {

    protected boolean htmlReportEnabled = true

    static FindbugsConfigurator create(Project project,
                                       Violations violations,
                                       Task evaluateViolations) {
        return new FindbugsConfigurator(project, violations, evaluateViolations)
    }

    private FindbugsConfigurator(Project project,
                                 Violations violations,
                                 Task evaluateViolations) {
        super(project, violations, evaluateViolations)
    }

    @Override
    protected String getToolName() {
        'findbugs'
    }

    @Override
    protected def getToolPlugin() {
        QuietFindbugsPlugin
    }

    @Override
    protected Class<FindBugsExtension> getExtensionClass() {
        FindBugsExtension
    }

    @Override
    protected Class<FindBugs> getTaskClass() {
        FindBugs
    }

    @Override
    protected Action<FindBugsExtension> getDefaultConfiguration() {
        return { extension ->
            extension.ext.htmlReportEnabled = { boolean enabled -> this.htmlReportEnabled = enabled }
            extension.toolVersion = '3.0.1'
        }
    }

    @Override
    protected void configureAndroidWithVariants(DomainObjectSet variants) {
        if (configured) return

        variants.all { configureVariant(it) }
        variantFilter.filteredTestVariants.all { configureVariant(it) }
        variantFilter.filteredUnitTestVariants.all { configureVariant(it) }
        configured = true
    }

    @Override
    protected void configureVariant(variant) {
        createToolTaskForAndroid(variant)
        def collectViolations = createCollectViolations(getToolTaskNameFor(variant), violations)
        evaluateViolations.dependsOn collectViolations
    }

    @Override
    protected void createToolTaskForAndroid(variant) {
        createTask(project, getToolTaskNameFor(variant), QuietFindbugsPlugin.Task) { task ->
            List<File> androidSourceDirs = variant.sourceSets.collect { it.javaDirectories }.flatten()
            task.description = "Run FindBugs analysis for ${variant.name} classes"
            task.source = androidSourceDirs
            task.classpath = variant.javaCompile.classpath
            task.extraArgs '-auxclasspath', androidJar
            task.conventionMapping.map("classes") {
                List<String> includes = createIncludePatterns(task.source, androidSourceDirs)
                getAndroidClasses(javaCompile(variant), includes)
            }
            sourceFilter.applyTo(task)
            task.dependsOn javaCompile(variant)
        }
    }

    private FileCollection getAndroidClasses(javaCompile, List<String> includes) {
        includes.isEmpty() ? project.files() : project.fileTree(javaCompile.destinationDir).include(includes) as ConfigurableFileTree
    }

    @Override
    protected void configureJavaProject() {
        super.configureJavaProject()
        project.afterEvaluate {
            project.sourceSets.each { SourceSet sourceSet ->
                String taskName = sourceSet.getTaskName(toolName, null)
                configureNamed(project, taskName) { task ->
                    task.conventionMapping.map("classes") {
                        List<File> sourceDirs = sourceSet.allJava.srcDirs.findAll { it.exists() }.toList()
                        List<String> includes = createIncludePatterns(task.source, sourceDirs)
                        getJavaClasses(sourceSet, includes)
                    }
                }
            }
        }
    }

    private static List<String> createIncludePatterns(FileCollection sourceFiles, List<File> sourceDirs) {
        List<Path> includedSourceFilesPaths = sourceFiles.matching { '**/*.java' }.files.collect { it.toPath() }
        List<Path> sourceDirsPaths = sourceDirs.collect { it.toPath() }
        createRelativePaths(includedSourceFilesPaths, sourceDirsPaths)
                .collect { Path relativePath -> (relativePath as String) - '.java' + '*' }
    }

    private static List<Path> createRelativePaths(List<Path> includedSourceFiles, List<Path> sourceDirs) {
        includedSourceFiles.collect { Path sourceFile ->
            sourceDirs
                    .findAll { Path sourceDir -> sourceFile.startsWith(sourceDir) }
                    .collect { Path sourceDir -> sourceDir.relativize(sourceFile) }
        }
                .flatten()
    }

    private FileCollection getJavaClasses(SourceSet sourceSet, List<String> includes) {
        includes.isEmpty() ? project.files() : createClassesTreeFrom(sourceSet, includes)
    }

    private FileCollection createClassesTreeFrom(SourceSet sourceSet, List<String> includes) {
        return sourceSet.output.classesDirs.inject(null) { ConfigurableFileTree cumulativeTree, File classesDir ->
            def tree = project.fileTree(classesDir)
                    .builtBy(sourceSet.output)
                    .include(includes) as ConfigurableFileTree
            cumulativeTree?.plus(tree) ?: tree
        }
    }

    @Override
    protected void configureToolTask(FindBugs task) {
        super.configureToolTask(task)
        task.reports.xml.enabled = true
        task.reports.html.enabled = false
    }

    @Override
    protected def createCollectViolations(String taskName, Violations violations) {
        if (htmlReportEnabled) {
            createHtmlReportTask(taskName)
        }
        createTask(project, "collect${taskName.capitalize()}Violations", CollectFindbugsViolationsTask) { task ->
            def findbugs = project.tasks[taskName] as FindBugs
            task.xmlReportFile = findbugs.reports.xml.destination
            task.violations = violations

            if (htmlReportEnabled) {
                task.dependsOn project.tasks["generate${taskName.capitalize()}HtmlReport"]
            } else {
                task.dependsOn findbugs
            }
        }
    }


    private void createHtmlReportTask(String taskName) {
        createTask(project, "generate${taskName.capitalize()}HtmlReport", GenerateFindBugsHtmlReport) { GenerateFindBugsHtmlReport task ->
            def findbugs = project.tasks[taskName] as FindBugs
            task.xmlReportFile = findbugs.reports.xml.destination
            task.htmlReportFile = new File(task.xmlReportFile.absolutePath - '.xml' + '.html')
            task.classpath = findbugs.findbugsClasspath
            task.dependsOn findbugs
        }
    }

    private def getAndroidJar() {
        "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
    }
}
