package com.palominolabs.config;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import org.skife.config.ConfigurationObjectFactory;

/**
 * Guice module that binds a ConfigurationObjectFactory.
 *
 * Create a ConfigModule using ConfigModuleBuilder, then use bindConfigBean from your own modules to bind any config
 * beans.
 */
public final class ConfigModule extends AbstractModule {

    private final ConfigurationObjectFactory configurationObjectFactory;

    ConfigModule(ConfigurationObjectFactory configurationObjectFactory) {
        this.configurationObjectFactory = configurationObjectFactory;
    }

    @Override
    protected void configure() {
        bind(ConfigurationObjectFactory.class).annotatedWith(ConfigInjectFactory.class)
            .toInstance(configurationObjectFactory);
    }

    /**
     * Binds a provider for the type configBeanType.
     *
     * This is intended to be called from other modules' configure() methods.
     *
     * @param binder         binder from caller module
     * @param configBeanType config bean type to bind
     * @param <T>            type of configBeanType
     */
    public static <T> void bindConfigBean(Binder binder, Class<T> configBeanType) {
        binder.bind(configBeanType).toProvider(new ConfigProvider<T>(configBeanType)).in(Scopes.SINGLETON);
    }

    /**
     * Instantiate a config bean directly.
     *
     * Useful for getting config objects during module initialization time.
     *
     * @param klass the bean class
     * @param <T>   klass's type
     * @return a populated config bean
     */
    public <T> T getConfigBean(Class<T> klass) {
        return configurationObjectFactory.build(klass);
    }

    private static class ConfigProvider<T> implements Provider<T> {

        @Inject
        @ConfigInjectFactory
        private ConfigurationObjectFactory beanFactory;

        private final Class<T> configClass;

        private ConfigProvider(Class<T> configClass) {
            this.configClass = configClass;
        }

        @Override
        public T get() {
            return beanFactory.build(configClass);
        }
    }
}
