package cn.srclink.service.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * MultiPluginClassLoaderImpl make multiple PluginClassLoaders as {@link PluginClassLoader}.</br>
 * If there are multiple plug-in directories, use the following code:
 * <blockquote><pre>
 *     PluginClassLoader plugLoader1 = new JarPluginClassLoaderImpl("plugins");
 *     PluginClassLoader plugLoader2 = new JarPluginClassLoaderImpl("extensions");
 *     PluginClassLoader loader = new MultiPluginClassLoaderImpl(plugLoader1, plugLoader2)
 * </pre></blockquote>
 */
public class MultiPluginClassLoaderImpl extends PluginClassLoader {
    private final List<PluginClassLoader> loaderList;

    public MultiPluginClassLoaderImpl(PluginClassLoader... loader) {
        loaderList = List.of(loader);
        reload();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (PluginClassLoader loader : loaderList) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (PluginClassLoader loader : loaderList) {
            URL url = loader.getResource(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Vector<URL> urls = new Vector<>();
        for (PluginClassLoader loader : loaderList) {
            Enumeration<URL> e = loader.getResources(name);
            while (e.hasMoreElements()) {
                urls.add(e.nextElement());
            }
        }
        return urls.elements();
    }

    @Override
    public Stream<URL> resources(String name) {
        List<URL> urls = new ArrayList<>();
        for (PluginClassLoader loader : loaderList) {
            urls.addAll(loader.resources(name).toList());
        }
        return urls.stream();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (PluginClassLoader loader : loaderList) {
            InputStream is = loader.getResourceAsStream(name);
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        for (PluginClassLoader loader : loaderList) {
            loader.setDefaultAssertionStatus(enabled);
        }
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        for (PluginClassLoader loader : loaderList) {
            loader.setPackageAssertionStatus(packageName, enabled);
        }
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        for (PluginClassLoader loader : loaderList) {
            loader.setClassAssertionStatus(className, enabled);
        }
    }

    @Override
    public void clearAssertionStatus() {
        for (PluginClassLoader loader : loaderList) {
            loader.clearAssertionStatus();
        }
    }

    @Override
    public String getName() {
        return "cn.srclink.service.plugin.MultiPluginClassLoaderImpl";
    }

    @Override
    public void reload() {
        for (PluginClassLoader loader : loaderList) {
            loader.reload();
        }
    }
}
