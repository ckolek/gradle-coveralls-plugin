package me.kolek.gradle.plugin.coveralls;

import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverageProvider;
import me.kolek.gradle.plugin.coveralls.coverage.jacoco.JacocoCoverageProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CoverallsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        CoverallsPluginExtension extension =
                project.getExtensions().create("coveralls", CoverallsPluginExtension.class, project.getObjects());

        tryCreateCoverageProvider(project, JacocoPluginExtension.class, this::createJacocoCoverageProvider)
                .ifPresent(extension::setCoverageProvider);

        project.getTasks().create("uploadCodeCoverage", CoverallsUpload.class, cu -> {});
    }

    private <E> Optional<CodeCoverageProvider> tryCreateCoverageProvider(Project project, Class<E> extensionClass,
            BiFunction<Project, E, CodeCoverageProvider> providerCreator) {
        E extension = project.getExtensions().findByType(extensionClass);
        if (extension != null) {
            return Optional.of(providerCreator.apply(project, extension));
        }
        return Optional.empty();
    }

    private JacocoCoverageProvider createJacocoCoverageProvider(Project project, JacocoPluginExtension jacocoExtension) {
        JacocoCoverageProvider coverageProvider = project.getObjects().newInstance(JacocoCoverageProvider.class);
        coverageProvider.setReportsDir(jacocoExtension.getReportsDir());
        return coverageProvider;
    }
}
