package me.kolek.gradle.plugin.coveralls.service;

public class TravisService implements CIService {
    private static final String TRAVIS = "TRAVIS";
    private static final String TRAVIS_BRANCH = "TRAVIS_BRANCH";
    private static final String TRAVIS_BUILD_DIR = "TRAVIS_BUILD_DIR";
    private static final String TRAVIS_BUILD_ID = "TRAVIS_BUILD_ID";
    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS_BUILD_NUMBER";
    private static final String TRAVIS_COMMIT = "TRAVIS_COMMIT";
    private static final String TRAVIS_COMMIT_MESSAGE = "TRAVIS_COMMIT_MESSAGE";
    private static final String TRAVIS_COMMIT_RANGE = "TRAVIS_COMMIT_RANGE";
    private static final String TRAVIS_EVENT_TYPE = "TRAVIS_EVENT_TYPE";
    private static final String TRAVIS_JOB_ID = "TRAVIS_JOB_ID";
    private static final String TRAVIS_JOB_NUMBER = "TRAVIS_JOB_NUMBER";
    private static final String TRAVIS_PULL_REQUEST = "TRAVIS_PULL_REQUEST";
    private static final String TRAVIS_PULL_REQUEST_BRANCH = "TRAVIS_PULL_REQUEST_BRANCH";
    private static final String TRAVIS_PULL_REQUEST_SHA = "TRAVIS_PULL_REQUEST_SHA";
    private static final String TRAVIS_PULL_REQUEST_SLUG = "TRAVIS_PULL_REQUEST_SLUG";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS_REPO_SLUG";
    private static final String TRAVIS_TAG = "TRAVIS_TAG";

    private boolean isPro;

    public boolean isPro() {
        return isPro;
    }

    public void setPro(boolean pro) {
        isPro = pro;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return "true".equalsIgnoreCase(System.getenv(TRAVIS));
    }

    @Override
    public String getName() {
        return isPro ? "travis-pro" : "travis-ci";
    }

    @Override
    public String getBuildNumber() {
        return System.getenv(TRAVIS_BUILD_NUMBER);
    }

    @Override
    public String getJobId() {
        return System.getenv(TRAVIS_JOB_ID);
    }

    @Override
    public String getPullRequest() {
        return System.getenv(TRAVIS_PULL_REQUEST);
    }
}
