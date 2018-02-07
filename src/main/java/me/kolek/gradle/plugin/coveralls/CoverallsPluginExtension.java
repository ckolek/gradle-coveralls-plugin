package me.kolek.gradle.plugin.coveralls;

import groovy.lang.Closure;
import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverageProvider;
import me.kolek.gradle.plugin.coveralls.coverage.jacoco.JacocoCoverageProvider;
import me.kolek.gradle.plugin.coveralls.service.CIService;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoverallsPluginExtension {
    private final ObjectFactory objectFactory;
    private final Property<String> repoToken;
    private final Property<CodeCoverageProvider> coverageProvider;
    private final CIServiceContainer serviceContainer;

    @Inject
    public CoverallsPluginExtension(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.repoToken = objectFactory.property(String.class);
        this.coverageProvider = objectFactory.property(CodeCoverageProvider.class);
        this.serviceContainer = objectFactory.newInstance(CIServiceContainer.class, objectFactory);
    }

    public String getRepoToken() {
        return repoToken.getOrNull();
    }

    public void setRepoToken(String repoToken) {
        this.repoToken.set(repoToken);
    }

    public CodeCoverageProvider getCoverageProvider() {
        return coverageProvider.getOrNull();
    }

    public void setCoverageProvider(CodeCoverageProvider coverageProvider) {
        this.coverageProvider.set(coverageProvider);
    }

    public List<CIService> getServices() {
        return serviceContainer.getServices();
    }

    public void services(Action<? super CIServiceContainer> action) {
        action.execute(serviceContainer);
    }

    public <C extends CodeCoverageProvider> C coverage(Class<C> coverageProviderType) {
        return createCoverageProvider(coverageProviderType);
    }

    public <C extends CodeCoverageProvider> C coverage(Class<C> coverageProviderType, Action<? super C> action) {
        C coverageProvider = createCoverageProvider(coverageProviderType);
        action.execute(coverageProvider);
        return coverageProvider;
    }

    public JacocoCoverageProvider jacoco() {
        return coverage(JacocoCoverageProvider.class);
    }

    public JacocoCoverageProvider jacoco(Action<? super JacocoCoverageProvider> action) {
        return coverage(JacocoCoverageProvider.class, action);
    }

    private <C extends CodeCoverageProvider> C createCoverageProvider(Class<C> coverageProviderType) {
        C coverageProvider = objectFactory.newInstance(coverageProviderType);
        setCoverageProvider(coverageProvider);
        return coverageProvider;
    }
}
