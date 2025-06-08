package cn.srclink.service.plugin;

public abstract class PluginClassLoader extends ClassLoader {
    /**
     * Reload the instance of PluginClassLoader.
     * When plugins are added or replaced,
     * the file cache should be updated accordingly.
     */
    public abstract void reload();
}
