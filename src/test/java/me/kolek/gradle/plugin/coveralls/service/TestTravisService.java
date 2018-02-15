package me.kolek.gradle.plugin.coveralls.service;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestTravisService extends CIServiceTest<TravisService> {
    private final boolean pro;

    public TestTravisService(boolean available, String name, String buildNumber, String jobId,
            String pullRequest, boolean pro) {
        super(true, available, name, buildNumber, jobId, pullRequest);
        this.pro = pro;
    }

    @Override
    public void setUp() {
        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("TRAVIS", Boolean.toString(available));
        environment.put("TRAVIS_BUILD_NUMBER", buildNumber);
        environment.put("TRAVIS_JOB_ID", jobId);
        environment.put("TRAVIS_PULL_REQUEST", pullRequest);

        service = new TravisService(environment);
        service.setPro(pro);
    }

    @Parameterized.Parameters
    public static Object[][] getParameters() {
        return new Object[][] {
                {true, "travis-ci", "10012", "A-1", null, false},
                {true, "travis-ci", "10012", "A-1", "114", false},
                {false, "travis-ci", "10013", "A-2", "115", false},
                {true, "travis-pro", "10014", "A-3", null, true},
                {true, "travis-pro", "10014", "A-3", "116", true},
                {false, "travis-pro", "10015", "A-4", "117", true}
        };
    }
}
