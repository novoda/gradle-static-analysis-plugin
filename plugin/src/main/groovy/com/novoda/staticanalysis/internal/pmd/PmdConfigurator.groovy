package com.novoda.staticanalysis.internal.pmd

import com.novoda.staticanalysis.Violations
import com.novoda.staticanalysis.internal.CodeQualityConfigurator
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

import static com.novoda.staticanalysis.internal.TasksCompat.createTask

class PmdConfigurator extends CodeQualityConfigurator<Pmd, PmdExtension> {

    private final configuredSourceSets = new HashSet<String>()

    static PmdConfigurator create(Project project,
                                  NamedDomainObjectContainer<Violations> violationsContainer,
                                  Task evaluateViolations) {
        Violations violations = violationsContainer.maybeCreate('PMD')
        return new PmdConfigurator(project, violations, evaluateViolations)
    }

    private PmdConfigurator(Project project,
                            Violations violations,
                            Task evaluateViolations) {
        super(project, violations, evaluateViolations)
    }

    @Override
    protected String getToolName() {
        'pmd'
    }

    @Override
    protected Class<PmdExtension> getExtensionClass() {
        PmdExtension
    }

    @Override
    protected Class<Pmd> getTaskClass() {
        Pmd
    }

    @Override
    protected Action<PmdExtension> getDefaultConfiguration() {
        return { extension ->
            extension.toolVersion = '5.5.1'
            extension.rulePriority = 5
        }
    }

    @Override
    protected void createToolTaskForAndroid(sourceSet) {
        def taskName = getToolTaskNameFor(sourceSet)
        if (configuredSourceSets.contains(taskName)) {
            return
        }
        configuredSourceSets.add(taskName)
        createTask(project, taskName, Pmd) { Pmd task ->
            task.description = "Run PMD analysis for ${sourceSet.name} classes"
            task.source = sourceSet.java.srcDirs
            task.exclude '**/*.kt'
            sourceFilter.applyTo(task)
        }
    }

    @Override
    protected def createCollectViolations(String taskName, Violations violations) {
        createTask(project, "collect${taskName.capitalize()}Violations", CollectPmdViolationsTask) { task ->
            def pmd = project.tasks[taskName] as Pmd
            task.xmlReportFile = pmd.reports.xml.destination
            task.violations = violations
            task.dependsOn(pmd)
        }
    }
}
