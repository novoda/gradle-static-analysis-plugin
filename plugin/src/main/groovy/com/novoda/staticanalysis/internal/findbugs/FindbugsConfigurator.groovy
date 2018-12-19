package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.tasks.SourceSet

import java.nio.file.Path

class FindbugsConfigurator extends CodeQualityConfigurator<FindBugs, FindBugsExtension> {

    protected boolean htmlReportEnabled = true

    static FindbugsConfigurator create(Project project,
                                       NamedDomainObjectContainer<Violations> violationsContainer,
                                       Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('Findbugs')
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
    protected Object getToolPlugin() {
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
    protected void configureAndroidVariant(variant) {
        FindBugs task = project.tasks.maybeCreate("findbugs${variant.name.capitalize()}", QuietFindbugsPlugin.Task)
        List<File> androidSourceDirs = variant.sourceSets.collect { it.javaDirectories }.flatten()
        task.with {
            description = "Run FindBugs analysis for ${variant.name} classes"
            source = androidSourceDirs
            classpath = variant.javaCompile.classpath
            extraArgs '-auxclasspath', androidJar
            exclude '**/*.kt'
        }
        sourceFilter.applyTo(task)
        task.conventionMapping.map("classes") {
            List<String> includes = createIncludePatterns(task.source, androidSourceDirs)
            getAndroidClasses(variant, includes)
        }
        task.dependsOn variant.javaCompile
    }

    private FileCollection getAndroidClasses(Object variant, List<String> includes) {
        includes.isEmpty() ? project.files() : project.fileTree(variant.javaCompile.destinationDir).include(includes)
    }

    @Override
    protected void configureJavaProject() {
        project.afterEvaluate {
            project.sourceSets.each { SourceSet sourceSet ->
                String taskName = sourceSet.getTaskName(toolName, null)
                FindBugs task = project.tasks.findByName(taskName)
                if (task != null) {
                    sourceFilter.applyTo(task)
                    task.conventionMapping.map("classes", {
                        List<File> sourceDirs = sourceSet.allJava.srcDirs.findAll { it.exists() }.toList()
                        List<String> includes = createIncludePatterns(task.source, sourceDirs)
                        getJavaClasses(sourceSet, includes)
                    })
                    task.exclude '**/*.kt'
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
        includes.isEmpty() ? project.files() : createClassesTreeFrom(sourceSet).include(includes)
    }

    /**
     * The simple "classes = sourceSet.output" may lead to non-existing resources directory
     * being passed to FindBugs Ant task, resulting in an error
     * */
    private ConfigurableFileTree createClassesTreeFrom(SourceSet sourceSet) {
        project.fileTree(sourceSet.output.classesDir, {
            it.builtBy(sourceSet.output)
        })
    }

    @Override
    protected void configureReportEvaluation(FindBugs findBugs, Violations violations) {
        findBugs.ignoreFailures = true
        findBugs.reports.xml.enabled = true
        findBugs.reports.html.enabled = false

        def collectViolations = createViolationsCollectionTask(findBugs, violations)
        evaluateViolations.dependsOn collectViolations

        if (htmlReportEnabled) {
            def generateHtmlReport = createHtmlReportTask(findBugs, collectViolations.xmlReportFile, collectViolations.htmlReportFile)
            collectViolations.dependsOn generateHtmlReport
            generateHtmlReport.dependsOn findBugs
        } else {
            collectViolations.dependsOn findBugs
        }
    }

    private CollectFindbugsViolationsTask createViolationsCollectionTask(FindBugs findBugs, Violations violations) {
        def task = project.tasks.maybeCreate("collect${findBugs.name.capitalize()}Violations", CollectFindbugsViolationsTask)
        task.xmlReportFile = findBugs.reports.xml.destination
        task.violations = violations
        task
    }

    private GenerateFindBugsHtmlReport createHtmlReportTask(FindBugs findBugs, File xmlReportFile, File htmlReportFile) {
        def task = project.tasks.maybeCreate("generate${findBugs.name.capitalize()}HtmlReport", GenerateFindBugsHtmlReport)
        task.xmlReportFile = xmlReportFile
        task.htmlReportFile = htmlReportFile
        task.classpath = findBugs.findbugsClasspath
        task.onlyIf { xmlReportFile?.exists() }
        task
    }

    private def getAndroidJar() {
        "${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar"
    }
}
