# Service API模块

Service API模块通过重定义 `ServiceLoader` 类，
引入 `PluginClassLoader` 实现简单的外部JAR插件加载功能。

## 使用示例

### 与 Spring 项目集成

您可以通过创建 `ServiceProvider` 实现类，
从 `Spring 上下文` 中提供指定类实例。

```java

@Component
public class SpringServiceProvider implements ServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(SpringServiceProvider.class);
    private final ApplicationContext applicationContext;

    public SpringServiceProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <S> Collection<S> load(Class<S> clazz) {
        Map<String, S> map = applicationContext.getBeansOfType(clazz);
        return map.values();
    }
}
```

在启动Spring项目时，
您可以监听 `ApplicationStartedEvent` 事件，
并通过以下代码配置 `ServiceProvider`：

```java

@Component
public class SystemInitializeEvent implements ApplicationListener<ApplicationStartedEvent>, Serializable {
    private final SpringServiceProvider springServiceProvider;

    public SystemInitializeEvent(SpringServiceProvider springServiceProvider) {
        this.springServiceProvider = springServiceProvider;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationStartedEvent event) {
        CustomizedServiceLoader.addProvider(springServiceProvider);
    }
}
```

## 更新日志

### 2025.06.13

- 为 `JarPluginClassLoaderImpl` 添加无父类的构造函数
- 为 `CustomizedServiceLoader` 添加获取和修改 `PluginClassLoader` 的方法
- 为 `CustomizedServiceLoader` 添加移除 `ServiceProvider` 的方法

### 2025.06.08

- 重构 `PluginClassLoader`，现在属于 `cn.srclink.service.plugin` 模块
- 新增 `MultiPluginClassLoader`，允许加载多个 `PluginClassLoader` 实例

### 2025.05.31

- 从UMS项目中提取的首个版本，支持从 `plugins` 目录加载jar插件
- 兼容原生Java SPI，调用方式与SPI的 `ServiceLoader` 一致
- 支持注册自定义 `ServiceProvider`