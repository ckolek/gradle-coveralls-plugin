package me.kolek.gradle.plugin.coveralls.service;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestCodeshipService extends CIServiceTest<CodeshipService> {
    private final String pullRequest;

    public TestCodeshipService(boolean available, String name, String buildNumber, String pullRequest) {
        super(true, available, name, buildNumber, null, "false".equals(pullRequest) ? null : pullRequest);
        this.pullRequest = pullRequest;
    }

    @Override
    public void setUp() {
        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("CODESHIP", Boolean.toString(available));
        environment.put("CI_NAME", name);
        environment.put("CI_BUILD_NUMBER", buildNumber);
        environment.put("CI_PULL_REQUEST", pullRequest);

        service = new CodeshipService(environment);
    }

    @Parameterized.Parameters
    public static Object[][] getParameters() {
        return new Object[][] {
                {true, "codeship", "10012", "false"},
                {false, "codeship", "10013", "false"},
                {true, "codeship", "10012", "115"},
                {false, "codeship", "10013", "115"}
        };
    }
}
