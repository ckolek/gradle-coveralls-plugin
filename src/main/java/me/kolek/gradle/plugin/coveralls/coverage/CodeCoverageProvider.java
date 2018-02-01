package me.kolek.gradle.plugin.coveralls.coverage;

import org.gradle.api.Task;

import java.util.Optional;

public interface CodeCoverageProvider {
    Optional<CodeCoverage> getCodeCoverage(Task task) throws Exception;
}
