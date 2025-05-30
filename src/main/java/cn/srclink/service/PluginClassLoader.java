package cn.srclink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

public class PluginClassLoader extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(PluginClassLoader.class);
    private transient final ClassLoader parent;
    private transient ClassLoader delegate;

    public PluginClassLoader(ClassLoader parent) {
        this.parent = parent;
        reload();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return delegate.getResource(name);
    }

    @Override
    public String getName() {
        return "cn.srclink.service.PluginClassLoader";
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return delegate.getResources(name);
    }

    @Override
    public Stream<URL> resources(String name) {
        return delegate.resources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return delegate.getResourceAsStream(name);
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        delegate.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        delegate.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        delegate.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        delegate.clearAssertionStatus();
    }

    public final void reload() {
        // TODO: use customized plugin directory
        File pluginDir = new File("plugins");
        File[] plugins = pluginDir.listFiles();
        List<URL> urls = new ArrayList<>();
        if (plugins != null) {
            Arrays.stream(plugins).filter(plugin -> plugin.getName().endsWith(".jar")).forEach(plugin -> {
                try {
                    urls.add(plugin.toURI().toURL());
                } catch (MalformedURLException e) {
                    log.error("Load plugin error: {}",e.getMessage());
                }
            });
        }
        delegate = new URLClassLoader(urls.toArray(new URL[0]), this.parent);
    }
}
