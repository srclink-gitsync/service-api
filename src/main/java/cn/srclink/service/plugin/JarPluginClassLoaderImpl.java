package cn.srclink.service.plugin;

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

/**
 * Support jar packaged plugins load from disk.
 *
 * @author srclink
 * @version 1.0.0
 */
public class JarPluginClassLoaderImpl extends PluginClassLoader {
    private static final Logger log = LoggerFactory.getLogger(JarPluginClassLoaderImpl.class);
    private transient final ClassLoader parent;
    private final File pluginDir;
    private transient ClassLoader delegate;

    public JarPluginClassLoaderImpl(File pluginDir) {
        this(JarPluginClassLoaderImpl.class.getClassLoader(), pluginDir);
    }

    public JarPluginClassLoaderImpl(ClassLoader parent, File pluginDir) {
        this.parent = parent;
        this.pluginDir = pluginDir;
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
        return "cn.srclink.service.plugin.JarPluginClassLoaderImpl";
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

    @Override
    public final void reload() {
        // rebuild the file list in plugin dir
        File[] plugins = pluginDir.listFiles();
        // build the external plugin class path
        List<URL> urls = new ArrayList<>();
        // check if the file list exists
        if (plugins != null) {
            // add all jar packaged plugins to classpath
            Arrays.stream(plugins).filter(plugin -> plugin.getName().endsWith(".jar")).forEach(plugin -> {
                try {
                    urls.add(plugin.toURI().toURL());
                } catch (MalformedURLException e) {
                    log.error("Load plugin error: {}", e.getMessage());
                }
            });
        }
        // recreate class loader, classes will be auto unload after gc
        delegate = new URLClassLoader(urls.toArray(new URL[0]), this.parent);
    }
}
