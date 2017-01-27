package com.novoda.staticanalysis.internal.checkstyle

import com.novoda.staticanalysis.EvaluateViolationsTask
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import com.novoda.staticanalysis.internal.QuietLogger
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.TemporaryFileProvider
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.TaskDependency
import org.gradle.internal.logging.ConsoleRenderer

class CheckstyleConfigurator extends CodeQualityConfigurator<Checkstyle, CheckstyleExtension> {

    public static final String UTF_8 = 'UTF-8'
    private final TemporaryFileProvider temporaryFileProvider

    CheckstyleConfigurator(Project project, EvaluateViolationsTask evaluateViolationsTask, TemporaryFileProvider temporaryFileProvider) {
        super(project, evaluateViolationsTask.maybeCreate('Checkstyle'), evaluateViolationsTask)
        this.temporaryFileProvider = temporaryFileProvider
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

                def url = 'com/novoda/staticanalysis/internal/checkstyle/modules.xml'
                def stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(url)

                def tokens = url.tokenize('/')
                def fileName = tokens[-1]
                String[] filePath = tokens.split({ it != fileName })[0] as String[]
                File file = temporaryFileProvider.createTemporaryFile('temp-', "-${fileName}", filePath);
                file.write(stream.text, UTF_8)

                checkstyleExtension.config = new TextResource() {
                    @Override
                    String asString() {
                        file.text
                    }

                    @Override
                    Reader asReader() {
                        new FileInputStream(file)
                    }

                    @Override
                    File asFile(String charset) {
                        throw new UnsupportedOperationException("Cannot create file for $url")
                    }

                    @Override
                    File asFile() {
                        file
                    }

                    @Override
                    Object getInputProperties() {
                        null
                    }

                    @Override
                    FileCollection getInputFiles() {
                        null
                    }

                    @Override
                    TaskDependency getBuildDependencies() {
                        new TaskDependency() {
                            @Override
                            Set<? extends Task> getDependencies(Task task) {
                                Collections.emptySet()
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Class<Checkstyle> getTaskClass() {
        Checkstyle
    }

    @Override
    protected void configureAndroid(Object variants) {
        project.with {
            android.sourceSets.all { sourceSet ->
                def sourceDirs = sourceSet.java.srcDirs
                def notEmptyDirs = sourceDirs.findAll { it.list()?.length > 0 }
                if (notEmptyDirs.empty) {
                    return
                }
                Checkstyle checkstyle = tasks.create("checkstyle${sourceSet.name.capitalize()}", Checkstyle)
                checkstyle.with {
                    description = "Run Checkstyle analysis for ${sourceSet.name} classes"
                    source = sourceSet.java.srcDirs
                    classpath = files("$buildDir/intermediates/classes/")
                }
                variants.all { variant ->
                    checkstyle.mustRunAfter variant.javaCompile
                }
            }
        }
    }

    @Override
    protected void configureTask(Checkstyle checkstyle) {
        super.configureTask(checkstyle)
        checkstyle.showViolations = false
        checkstyle.ignoreFailures = true
        checkstyle.metaClass.getLogger = { QuietLogger.INSTANCE }
        checkstyle.doLast {
            File xmlReportFile = checkstyle.reports.xml.destination
            File htmlReportFile = new File(xmlReportFile.absolutePath - '.xml' + '.html')

            GPathResult xml = new XmlSlurper().parse(xmlReportFile)
            int errors = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'error' }.size()
            int warnings = xml.'**'.findAll { node -> node.name() == 'error' && node.@severity == 'warning' }.size()
            String reportUrl = new ConsoleRenderer().asClickableFileUrl(htmlReportFile ?: xmlReportFile)
            violations.addViolations(errors, warnings, reportUrl)
        }
        evaluateViolations.dependsOn checkstyle
    }
}
