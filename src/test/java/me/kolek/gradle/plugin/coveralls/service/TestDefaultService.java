package me.kolek.gradle.plugin.coveralls.service;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestDefaultService extends CIServiceTest<DefaultService> {
    public TestDefaultService(String name, String buildNumber, String jobId, String pullRequest) {
        super(false, true, name, buildNumber, jobId, pullRequest);
    }

    @Override
    public void setUp() {
        service = new DefaultService();
        service.setName(name);
        service.setBuildNumber(buildNumber);
        service.setJobId(jobId);
        service.setPullRequest(pullRequest);
    }

    @Parameterized.Parameters
    public static Object[][] getParameters() {
        return new Object[][] {
                {"TestService", "10012", "A-1", "114"}
        };
    }
}
