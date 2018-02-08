package me.kolek.gradle.plugin.coveralls;

import groovy.lang.Closure;
import me.kolek.gradle.plugin.coveralls.service.CIService;
import me.kolek.gradle.plugin.coveralls.service.CodeshipService;
import me.kolek.gradle.plugin.coveralls.service.DefaultService;
import me.kolek.gradle.plugin.coveralls.service.TravisService;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CIServiceContainer {
    private final ObjectFactory objectFactory;
    private final ListProperty<CIService> services;

    @Inject
    public CIServiceContainer(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.services = objectFactory.listProperty(CIService.class);
    }

    public List<CIService> getServices() {
        return services.getOrElse(Collections.emptyList());
    }

    public void setServices(List<CIService> services) {
        this.services.set(services);
    }

    public void addService(CIService service) {
        List<CIService> services;
        if (this.services.isPresent()) {
            services = new ArrayList<>(this.services.get());
        } else {
            services = new ArrayList<>();
        }
        services.add(service);
        this.services.set(services);
    }

    public <S extends CIService> S service(Class<S> serviceType) {
        return createService(serviceType);
    }

    public <S extends CIService> S service(Class<S> serviceType, Action<? super S> action) {
        S service = createService(serviceType);
        action.execute(service);
        return service;
    }

    public CodeshipService codeship() {
        return service(CodeshipService.class);
    }

    public CodeshipService codeship(Action<? super CodeshipService> action) {
        return service(CodeshipService.class, action);
    }

    public TravisService travis() {
        return service(TravisService.class);
    }

    public TravisService travis(Action<? super TravisService> action) {
        return service(TravisService.class, action);
    }

    public DefaultService custom() {
        return service(DefaultService.class);
    }

    public DefaultService custom(Action<? super DefaultService> action) {
        return service(DefaultService.class, action);
    }

    private <S extends CIService> S createService(Class<S> serviceType) {
        S service = objectFactory.newInstance(serviceType);
        addService(service);
        return service;
    }
}
