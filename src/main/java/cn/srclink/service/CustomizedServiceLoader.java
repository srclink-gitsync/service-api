package cn.srclink.service;

import cn.srclink.service.plugin.JarPluginClassLoaderImpl;
import cn.srclink.service.plugin.MultiPluginClassLoaderImpl;
import cn.srclink.service.plugin.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public final class CustomizedServiceLoader<S> implements Iterable<S>, Serializable {
    private final static List<ServiceProvider> providerList = new ArrayList<>();
    private final static Logger log = LoggerFactory.getLogger(CustomizedServiceLoader.class);
    private static PluginClassLoader loader =
            new JarPluginClassLoaderImpl(new File("plugins"));
    // TODO: use customized ServiceLoader(SPI)
    private final ServiceLoader<S> serviceLoader;
    private final Class<S> serviceClass;
    private List<S> cachedServices;

    public CustomizedServiceLoader(Class<S> serviceClass, PluginClassLoader pluginClassLoader) {
        this.serviceLoader = ServiceLoader.load(serviceClass, pluginClassLoader);
        this.serviceClass = serviceClass;
    }

    public CustomizedServiceLoader(Class<S> serviceClass) {
        this(serviceClass, loader);
    }

    /**
     * Get the default {@link PluginClassLoader} when creating a {@link ServiceLoader}.
     * By default, {@link JarPluginClassLoaderImpl} is used with the plug-in directory "plugins".
     *
     * @return {@link PluginClassLoader} instance
     */
    public static PluginClassLoader getLoader() {
        return loader;
    }

    /**
     * Set the default {@link PluginClassLoader} when creating a {@link ServiceLoader}.
     * If you use a multiple plug-in directory, see {@link MultiPluginClassLoaderImpl}
     *
     * @param loader The {@link PluginClassLoader} instance.
     */
    public static void setLoader(PluginClassLoader loader) {
        CustomizedServiceLoader.loader = loader;
    }

    public static <S> CustomizedServiceLoader<S> load(Class<S> serviceClass) {
        return new CustomizedServiceLoader<>(serviceClass);
    }

    public static <S> CustomizedServiceLoader<S> load(Class<S> serviceClass, PluginClassLoader loader) {
        return new CustomizedServiceLoader<>(serviceClass, loader);
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
