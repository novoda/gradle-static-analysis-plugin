package com.novoda.staticanalysis.internal.findbugs

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.internal.logging.ConsoleRenderer

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
    protected void configureAndroid(Object variants) {
        variants.all { variant ->
            FindBugs task = project.tasks.create("findbugs${variant.name.capitalize()}", QuietFindbugsPlugin.Task)
            task.with {
                group = "verification"
                description = "Run FindBugs analysis for ${variant.name} classes"
                source = variant.sourceSets.java.srcDirs.collect { it.path }.flatten()
                classes = project.fileTree(variant.javaCompile.destinationDir)
                classpath = variant.javaCompile.classpath
                dependsOn variant.javaCompile
            }
        }
    }

    @Override
    protected void configureTask(FindBugs findBugs) {
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
