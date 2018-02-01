package me.kolek.gradle.plugin.coveralls.service;

public interface CIService {
    boolean isSupported();

    boolean isAvailable();

    String getName();

    String getBuildNumber();

    String getJobId();

    String getPullRequest();
}
