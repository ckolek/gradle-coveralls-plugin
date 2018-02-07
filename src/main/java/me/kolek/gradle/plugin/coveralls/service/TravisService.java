package me.kolek.gradle.plugin.coveralls.service;

public class TravisService implements CIService {
    private static final String TRAVIS = "TRAVIS";
    private static final String TRAVIS_BRANCH = "TRAVIS";
    private static final String TRAVIS_BUILD_DIR = "TRAVIS";
    private static final String TRAVIS_BUILD_ID = "TRAVIS";
    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS";
    private static final String TRAVIS_COMMIT = "TRAVIS";
    private static final String TRAVIS_COMMIT_MESSAGE = "TRAVIS";
    private static final String TRAVIS_COMMIT_RANGE = "TRAVIS";
    private static final String TRAVIS_EVENT_TYPE = "TRAVIS";
    private static final String TRAVIS_JOB_ID = "TRAVIS";
    private static final String TRAVIS_JOB_NUMBER = "TRAVIS";
    private static final String TRAVIS_PULL_REQUEST = "TRAVIS";
    private static final String TRAVIS_PULL_REQUEST_BRANCH = "TRAVIS";
    private static final String TRAVIS_PULL_REQUEST_SHA = "TRAVIS";
    private static final String TRAVIS_PULL_REQUEST_SLUG = "TRAVIS";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS";
    private static final String TRAVIS_TAG = "TRAVIS";

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
