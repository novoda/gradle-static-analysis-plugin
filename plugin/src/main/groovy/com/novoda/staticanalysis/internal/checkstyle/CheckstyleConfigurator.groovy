package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import groovy.util.slurpersupport.GPathResult
import com.novoda.staticanalysis.internal.Violations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

class CheckstyleConfigurator extends CodeQualityConfigurator<Checkstyle, CheckstyleExtension> {

    CheckstyleConfigurator(Project project, EvaluateViolationsTask evaluateViolationsTask) {
        super(project, evaluateViolationsTask.maybeCreate('Checkstyle'), evaluateViolationsTask)
    }

    @Override
    protected String getToolName() {
        'checkstyle'
    }

    @Override
    protected Class<CheckstyleExtension> getExtensionClass() {
        CheckstyleExtension
    }

    @Override
    protected Action<CheckstyleExtension> getDefaultConfiguration() {
        new Action<CheckstyleExtension>() {
            @Override
            void execute(CheckstyleExtension checkstyleExtension) {
                checkstyleExtension.toolVersion = '7.1.2'
            }
        }
    }

    @Override
    protected Class<Checkstyle> getTaskClass() {
        Checkstyle
    }

    @Override
    protected void configureAndroidProject(NamedDomainObjectSet variants) {
        project.with {
            variants.all { variant ->
                variant.sourceSets.each { sourceSet ->
                    def taskName = "checkstyle${sourceSet.name.capitalize()}"
                    Checkstyle task = tasks.findByName(taskName)
                    if (task == null) {
                        task = tasks.create(taskName, Checkstyle)
                        task.with {
                            description = "Run Checkstyle analysis for ${sourceSet.name} classes"
                            source = sourceSet.java.srcDirs
                            classpath = files("$buildDir/intermediates/classes/")
                        }
                    }
                    sourceFilter.applyTo(task)
                    task.mustRunAfter variant.javaCompile
                }
            }
        }
    }

    @Override
    protected void configureReportEvaluation(Checkstyle checkstyle, Violations violations) {
        checkstyle.showViolations = false
        checkstyle.ignoreFailures = true
        checkstyle.metaClass.getLogger = { QuietLogger.INSTANCE }
        checkstyle.doLast {
            File xmlReportFile = checkstyle.reports.xml.destination
            File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')

            GPathResult xml = new XmlSlurper().parse(xmlReportFile)
            int errors = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'error' }.size()
            int warnings = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'warning' }.size()
            violations.addViolations(errors, warnings, htmlReportFile ?: xmlReportFile)
        }
        evaluateViolations.dependsOn checkstyle
    }
}
