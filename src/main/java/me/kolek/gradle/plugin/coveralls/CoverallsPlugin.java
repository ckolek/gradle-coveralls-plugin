package me.kolek.gradle.plugin.coveralls;

import me.kolek.gradle.plugin.coveralls.coverage.jacoco.JacocoCoverageProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

public class CoverallsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        CoverallsPluginExtension extension =
                project.getExtensions().create("coveralls", CoverallsPluginExtension.class, project.getObjects());

        JacocoPluginExtension jacocoExtension = project.getExtensions().findByType(JacocoPluginExtension.class);
        if (jacocoExtension != null) {
            extension.setCoverageProvider(createJacocoCoverageProvider(project, jacocoExtension));
        }

        project.getTasks().create("uploadCodeCoverage", CoverallsUpload.class, cu -> {

        });
    }

    private JacocoCoverageProvider createJacocoCoverageProvider(Project project, JacocoPluginExtension jacocoExtension) {
        JacocoCoverageProvider coverageProvider = project.getObjects().newInstance(JacocoCoverageProvider.class);
        coverageProvider.setReportsDir(jacocoExtension.getReportsDir());
        return coverageProvider;
    }
}
