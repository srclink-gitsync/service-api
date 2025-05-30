package cn.srclink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public final class CustomizedServiceLoader<S> implements Iterable<S>, Serializable {
    private final static List<ServiceProvider> providerList = new ArrayList<>();
    private final static Logger log = LoggerFactory.getLogger(CustomizedServiceLoader.class);
    // TODO: move PluginClassLoader to provider
    private final static PluginClassLoader loader =
            new PluginClassLoader(Thread.currentThread().getContextClassLoader());
    // TODO: use customized ServiceLoader(SPI)
    private final ServiceLoader<S> serviceLoader;
    private final Class<S> serviceClass;
    private List<S> cachedServices;

    public CustomizedServiceLoader(Class<S> serviceClass) {
        this.serviceLoader = ServiceLoader.load(serviceClass, loader);
        this.serviceClass = serviceClass;
    }

    public static <S> CustomizedServiceLoader<S> load(Class<S> serviceClass) {
        return new CustomizedServiceLoader<>(serviceClass);
    }

    public static void addProvider(ServiceProvider serviceProvider) {
        if (providerList.contains(serviceProvider)) {
            return;
        }
        providerList.add(serviceProvider);
    }

    public void reload() {
        loader.reload();
        serviceLoader.reload();
        cachedServices = null;
    }

    public Iterator<S> iterator() {
        if (cachedServices != null) {
            return cachedServices.iterator();
        }
        List<S> serviceList = new ArrayList<>();
        List<String> serviceNameList = new ArrayList<>();
        for (ServiceProvider serviceProvider : providerList) {
            for (S service : serviceProvider.load(serviceClass)) {
                if (!serviceNameList.contains(service.getClass().getName())) {
                    serviceList.add(service);
                    serviceNameList.add(service.getClass().getName());
                    log.debug(
                            "Loading {} from customize provider {}",
                            service.getClass().getName(),
                            serviceProvider.getClass().getName()
                    );
                }
            }
        }
        for (S service : serviceLoader) {
            if (!serviceNameList.contains(service.getClass().getName())) {
                serviceList.add(service);
                serviceNameList.add(service.getClass().getName());
            }
        }
        cachedServices = serviceList;
        return serviceList.iterator();
    }
}
