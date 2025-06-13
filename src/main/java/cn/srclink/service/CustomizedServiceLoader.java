package cn.srclink.service;

import cn.srclink.service.plugin.JarPluginClassLoaderImpl;
import cn.srclink.service.plugin.MultiPluginClassLoaderImpl;
import cn.srclink.service.plugin.PluginClassLoader;
import org.jetbrains.annotations.NotNull;
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
    private final ServiceLoader<S> serviceLoader;
    private final Class<S> serviceClass;
    private List<S> cachedServices;

    /**
     * Initialize {@link CustomizedServiceLoader} with {@link ServiceLoader}.
     *
     * @param serviceLoader The instance of {@link ServiceLoader}.
     * @param serviceClass  The {@link Class} reference of service.
     */
    public CustomizedServiceLoader(ServiceLoader<S> serviceLoader, Class<S> serviceClass) {
        this.serviceLoader = serviceLoader;
        this.serviceClass = serviceClass;
    }

    /**
     * Initialize with customized {@link PluginClassLoader}.
     *
     * @param serviceClass      The {@link Class} reference of service.
     * @param pluginClassLoader The instance of {@link PluginClassLoader}.
     */
    public CustomizedServiceLoader(Class<S> serviceClass, PluginClassLoader pluginClassLoader) {
        this(ServiceLoader.load(serviceClass, pluginClassLoader), serviceClass);
    }

    /**
     * Initialize with default {@link PluginClassLoader}.
     *
     * @param serviceClass The {@link Class} reference of service.
     */
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

    /**
     * Register runtime service provider to context.
     *
     * @param serviceProvider The service provider implement.
     */
    public static void addProvider(ServiceProvider serviceProvider) {
        if (providerList.contains(serviceProvider)) {
            return;
        }
        providerList.add(serviceProvider);
    }

    /**
     * Remove runtime service provider from context.
     *
     * @param serviceProvider The service provider implement.
     */
    public static void removeProvider(ServiceProvider serviceProvider) {
        providerList.remove(serviceProvider);
    }

    public void reload() {
        // reload classes
        loader.reload();
        // reload services
        serviceLoader.reload();
        // invalid cache
        cachedServices = null;
    }

    @NotNull
    @Override
    public Iterator<S> iterator() {
        Iterator<S> result;
        // lazy load, initialize cached list when access
        if (cachedServices == null) {
            List<S> serviceList = new ArrayList<>();
            List<String> serviceNameList = new ArrayList<>();
            // load from runtime provider first
            for (ServiceProvider serviceProvider : providerList) {
                for (S service : serviceProvider.load(serviceClass)) {
                    if (!serviceNameList.contains(service.getClass().getName())) {
                        serviceList.add(service);
                        serviceNameList.add(service.getClass().getName());
                        log.debug(
                                "Loading {}@{} by provider {}",
                                service.getClass().getName(),
                                Integer.toHexString(System.identityHashCode(service)),
                                serviceProvider.getClass().getName()
                        );
                    }
                }
            }
            // load by Java SPI
            for (S service : serviceLoader) {
                if (!serviceNameList.contains(service.getClass().getName())) {
                    serviceList.add(service);
                    serviceNameList.add(service.getClass().getName());
                }
            }
            cachedServices = serviceList;
        }
        result = cachedServices.iterator();
        return result;
    }
}
