package me.kolek.gradle.plugin.coveralls.service;

import java.util.Map;

public abstract class EnvironmentService implements CIService {
    private final Map<String, String> environment;

    protected EnvironmentService(Map<String, String> environment) {
        this.environment = environment;
    }

    protected String get(String key) {
        return environment.get(key);
    }

    protected boolean check(String key, String value) {
        return value.equals(environment.get(key));
    }
}
