import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

public class Libraries {
    private final Project project

    public Libraries(Project project) {
        this.project = project
    }

    public final String junit = 'junit:junit:4.12'
    public final String truth = 'com.google.truth:truth:0.30'
    public final String guava = 'com.google.guava:guava:19.0'
    public final Findbugs findbugs = new Findbugs()

    public Dependency getGradleApi() {
        project.dependencies.gradleApi()
    }

    public Dependency getGradleTestKit() {
        project.dependencies.gradleTestKit()
    }

    private static class Findbugs {
        public final String annotations = 'com.google.code.findbugs:jsr305:3.0.0'
    }
}
