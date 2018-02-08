package me.kolek.gradle.plugin.coveralls.service;

public class CodeshipService implements CIService {
    private static final String CODESHIP = "CODESHIP";
    private static final String CI = "CI";
    private static final String CI_BRANCH = "CI_BRANCH";
    private static final String CI_BUILD_NUMBER = "CI_BUILD_NUMBER";
    private static final String CI_BUILD_URL = "CI_BUILD_URL";
    private static final String CI_COMMITTER_EMAIL = "CI_COMMITTER_EMAIL";
    private static final String CI_COMMITTER_NAME = "CI_COMMITTER_NAME";
    private static final String CI_COMMITTER_USERNAME = "CI_COMMITTER_USERNAME";
    private static final String CI_COMMIT_ID = "CI_COMMIT_ID";
    private static final String CI_MESSAGE = "CI_MESSAGE";
    private static final String CI_NAME = "CI_NAME";
    private static final String CI_PULL_REQUEST = "CI_PULL_REQUEST";
    private static final String CI_REPO_NAME = "CI_REPO_NAME";

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return "true".equalsIgnoreCase(System.getenv(CODESHIP));
    }

    @Override
    public String getName() {
        return System.getenv(CI_NAME);
    }

    @Override
    public String getBuildNumber() {
        return null;
    }

    @Override
    public String getJobId() {
        return System.getenv(CI_BUILD_NUMBER);
    }

    @Override
    public String getPullRequest() {
        String pullRequest = System.getenv(CI_PULL_REQUEST);
        return "false".equalsIgnoreCase(pullRequest) ? null : pullRequest;
    }
}
