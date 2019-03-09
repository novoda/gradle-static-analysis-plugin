package com.novoda.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

import javax.annotation.Nullable

abstract class TestProject<T extends TestProject> {
    private static final Closure<String> EXTENSION_TEMPLATE = { TestProject project ->
        """
        staticAnalysis {
            ${(project.penalty ?: '').replace('            ', '')}
            ${(project.toolsConfig ?: '').replace('        ', '    ')}
        }
        ${project.additionalConfiguration}
        """.stripIndent()
    }
    private static final Closure<String> SETTINGS_GRADLE_TEMPLATE = { String projectName ->
        """
        rootProject.name = '$projectName'
        includeBuild('$Fixtures.ROOT_DIR/src/test/test-plugin') {
            dependencySubstitution {
                substitute module('com.novoda:gradle-static-analysis-plugin') with project(':')
            }
        }
        """
    }

    private final File projectDir
    private final Closure<String> template
    String additionalConfiguration = ''
    Map<String, List<File>> sourceSets = [main: []]
    List<String> plugins = []
    String penalty
    String toolsConfig = ''

    TestProject(Closure<String> template) {
        this.template = template
        this.projectDir = createProjectDir("${System.currentTimeMillis()}")
    }

    private static File createProjectDir(String path) {
        File dir = new File(Fixtures.BUILD_DIR, "test-projects/$path")
        dir.deleteDir()
        dir.mkdirs()
        return dir
    }

    List<String> defaultArguments() {
        ['--stacktrace']
    }

    T withFile(File source, String path) {
        write(source.text, path)
        return this
    }

    T withFile(String text, String path) {
        write(text, path)
        return this
    }

    private void write(String text, String path) {
        File file = new File(projectDir, path)
        file.parentFile.mkdirs()
        file.text = text
    }

    T copyIntoSourceSet(String sourceSet, File srcDir) {
        srcDir.listFiles().each {
            withFile(it, "src/${sourceSet}/java/${it.name}")
        }
        return this
    }

    T withSourceSet(String sourceSet, File... srcDirs) {
        sourceSets[sourceSet] = srcDirs
        return this
    }

    T withPenalty(String penalty) {
        this.penalty = "penalty $penalty"
        return this
    }

    T withToolsConfig(String toolsConfig) {
        this.toolsConfig = toolsConfig
        return this
    }

    T withAdditionalConfiguration(String additionalConfiguration) {
        this.additionalConfiguration = additionalConfiguration
        return this
    }

    T withPlugin(String plugin, String version = null) {
        this.plugins.add("id '$plugin' ${version ? "version '$version'" : ""}")
        return this
    }


    Result build(String... arguments) {
        BuildResult buildResult = newRunner(arguments).build()
        createResult(buildResult)
    }

    private GradleRunner newRunner(String... arguments) {
        new File(projectDir, 'build.gradle').text = template.call(this)
        new File(projectDir, 'settings.gradle').text = SETTINGS_GRADLE_TEMPLATE.call(projectDir.name)
        List<String> defaultArgs = defaultArguments()
        List<String> args = new ArrayList<>(arguments.size() + defaultArgs.size())
        args.addAll(defaultArgs)
        args.addAll(arguments)
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(args)
                .forwardOutput()
    }

    private createResult(BuildResult buildResult) {
        new Result(buildResult, new File(projectDir, 'build'))
    }

    Result buildAndFail(String... arguments) {
        BuildResult buildResult = newRunner(arguments).buildAndFail()
        createResult(buildResult)
    }

    void deleteDir() {
        recursiveDelete(projectDir)
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles()
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each)
            }
        }
        file.delete()
    }

    protected static String formatExtension(TestProject project) {
        EXTENSION_TEMPLATE.call(project)
    }

    protected static String formatPlugins(TestProject project) {
        project.plugins.join('\n')
    }

    static class Result {
        private final BuildResult buildResult
        private final File buildDir

        Result(BuildResult buildResult, File buildDir) {
            this.buildResult = buildResult
            this.buildDir = buildDir
        }

        Logs getLogs() {
            new Logs(buildResult.output)
        }

        String buildFileUrl(String path) {
            new File(buildDir, path).path
        }

        List<String> getTasksPaths() {
            buildResult.tasks.collect { it.path }
        }

        @Nullable
        TaskOutcome outcome(String taskPath) {
            buildResult.task(taskPath).outcome
        }

        static class Logs {
            final String output

            Logs(String output) {
                this.output = output
            }
        }
    }
}
