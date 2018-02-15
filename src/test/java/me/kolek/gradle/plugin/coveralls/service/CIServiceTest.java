package me.kolek.gradle.plugin.coveralls.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class CIServiceTest<S extends CIService> {
    protected final boolean supported;
    protected final boolean available;
    protected final String name;
    protected final String buildNumber;
    protected final String jobId;
    protected final String pullRequest;

    protected S service;

    protected CIServiceTest(boolean supported, boolean available, String name, String buildNumber, String jobId,
            String pullRequest) {
        this.supported = supported;
        this.available = available;
        this.name = name;
        this.buildNumber = buildNumber;
        this.jobId = jobId;
        this.pullRequest = pullRequest;
    }

    @Before
    public abstract void setUp();

    @Test
    public void test() {
        assertEquals(supported, service.isSupported());
        assertEquals(available, service.isAvailable());
        assertEquals(name, service.getName());
        assertEquals(buildNumber, service.getBuildNumber());
        assertEquals(jobId, service.getJobId());
        assertEquals(pullRequest, service.getPullRequest());
    }
}
