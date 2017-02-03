package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.logging.ConsoleRenderer

import java.nio.file.Path

class FindbugsConfigurator extends CodeQualityConfigurator<FindBugs, FindBugsExtension> {

    FindbugsConfigurator(Project project, EvaluateViolationsTask evaluateViolations) {
        super(project, evaluateViolations.maybeCreate('Findbugs'), evaluateViolations)
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
        new Action<FindBugsExtension>() {
            @Override
            void execute(FindBugsExtension findBugsExtension) {
                findBugsExtension.toolVersion = '3.0.1'
            }
        }
    }

    @Override
    protected void configureAndroidProject(Object variants) {
        variants.all { variant ->
            FindBugs task = project.tasks.create("findbugs${variant.name.capitalize()}", QuietFindbugsPlugin.Task)
            List<File> androidSourceDirs = variant.sourceSets.collect { it.javaDirectories }.flatten()
            task.with {
                description = "Run FindBugs analysis for ${variant.name} classes"
                source = androidSourceDirs
                classpath = variant.javaCompile.classpath
            }
            sourceFilter.applyTo(task)
            task.conventionMapping.map("classes", {
                List<String> includes = createIncludesPatternsFrom(task.source, androidSourceDirs)
                project.fileTree(variant.javaCompile.destinationDir).include(includes)
            }.memoize());
            task.dependsOn variant.javaCompile
        }
    }

    @Override
    protected void configureJavaProject() {
        project.sourceSets.each { SourceSet sourceSet ->
            String taskName = sourceSet.getTaskName(toolName, null)
            FindBugs task = project.tasks.findByName(taskName)
            if (task != null) {
                sourceFilter.applyTo(task)
                task.conventionMapping.map("classes", {
                    List<File> sourceDirs = sourceSet.allJava.srcDirs.findAll { it.exists() }.toList()
                    List<String> includes = createIncludesPatternsFrom(task.source, sourceDirs)
                    createClassesTreeFrom(sourceSet).include(includes)
                }.memoize());
            }
        }
    }

    private static List<String> createIncludesPatternsFrom(FileCollection sourceFiles, List<File> sourceDirs) {
        List<Path> includedSourceFiles = sourceFiles.matching { '**/*.java' }.files.collect { it.toPath() }
        createIncludePatterns(includedSourceFiles, sourceDirs.collect { it.toPath() })
    }

    private static List<String> createIncludePatterns(List<Path> includedSourceFiles, List<Path> sourceDirs) {
        createRelativePaths(includedSourceFiles, sourceDirs)
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
    protected void configureReportEvaluation(FindBugs findBugs) {
        findBugs.ignoreFailures = true
        findBugs.reports.xml.enabled = true
        findBugs.reports.html.enabled = false
        File xmlReportFile = findBugs.reports.xml.destination
        File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')
        findBugs.doLast {
            evaluateReports(xmlReportFile, htmlReportFile)
        }
        createHtmlReportTask(findBugs, xmlReportFile, htmlReportFile)
    }

    private void evaluateReports(File xmlReportFile, File htmlReportFile) {
        def evaluator = new FinbugsViolationsEvaluator(xmlReportFile)
        String reportUrl = new ConsoleRenderer().asClickableFileUrl(htmlReportFile)
        violations.addViolations(evaluator.errorsCount(), evaluator.warningsCount(), reportUrl)
    }

    private GenerateHtmlReport createHtmlReportTask(FindBugs findBugs, File xmlReportFile, File htmlReportFile) {
        project.tasks.create("generate${findBugs.name.capitalize()}HtmlReport", GenerateHtmlReport) { GenerateHtmlReport generateHtmlReport ->
            generateHtmlReport.xmlReportFile = xmlReportFile
            generateHtmlReport.htmlReportFile = htmlReportFile
            generateHtmlReport.classpath = findBugs.findbugsClasspath
            generateHtmlReport.dependsOn findBugs
            evaluateViolations.dependsOn generateHtmlReport
        }
    }

}
