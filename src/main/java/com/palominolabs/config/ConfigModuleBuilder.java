package com.palominolabs.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.skife.config.CommonsConfigSource;
import org.skife.config.ConfigurationObjectFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for ConfigModule instances.
 *
 * <p> For common cases of loading a UTF-8 encoded properties-formatted configuration source, there are convenience
 * methods for File, InputStream and URL. For more complex configuration needs, the addConfiguration method accepts any
 * commons-configuration Configuration object. </p>
 *
 * <p> Configuration sources can be combined to represent increasingly specific configuration sources. For instance, you
 * could have global, environment-specific, and instance-specific config files that could be layered like so:
 *
 * <pre>
 * {@code
 * configModuleBuilder.addPropertiesFile(new File("/path/to/global.properties"))
 *     .addPropertiesFile(new File("/path/to/environment.properties"))
 *     .addPropertiesFile(new File("/path/to/instance.properties"));
 * }
 * </pre>
 *
 * The config sources are treated like a stack, so the last-specified one has priority for any keys that are defined in
 * multiple config sources.
 *
 * <p> Any config keys that are not specified by any config source will use the defaults specified for the applicable
 * config-magic methods via {@code @Default} and {@code @DefaultNull} annotations. </p>
 */
@NotThreadSafe
public final class ConfigModuleBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ConfigModuleBuilder.class);

    private final Deque<Configuration> configStack = new ArrayDeque<Configuration>();

    /**
     * Add any form of Configuration to the config stack.
     *
     * You could use this to load configuration from system properties (SystemConfiguration), properties files
     * (PropertiesConfiguration), environment variables (EnvironmentConfiguration), etc.
     *
     * @param configuration configuration
     * @return this
     */
    public ConfigModuleBuilder addConfiguration(@Nonnull Configuration configuration) {
        configStack.push(configuration);
        return this;
    }

    /**
     * Add config from a File that represents a UTF-8 properties file to the config stack.
     *
     * @param configFile path to a UTF-8 properties file
     * @return this
     * @throws ConfigException if the file cannot be read
     */
    public ConfigModuleBuilder addPropertiesFile(File configFile) throws ConfigException {

        PropertiesConfiguration pc = getNewPropertiesConfig();

        try {
            pc.load(configFile);
        } catch (ConfigurationException e) {
            throw new ConfigException("Couldn't load file <" + configFile + ">", e);
        }

        configStack.push(pc);

        return this;
    }

    /**
     * Add config from a URL to UTF-8 properties data to the config stack.
     *
     * @param url url of UTF-8 properties data
     * @return this
     * @throws ConfigException if properties cannot be loaded
     */
    public ConfigModuleBuilder addPropertiesUrl(@Nonnull URL url) throws ConfigException {
        PropertiesConfiguration pc = getNewPropertiesConfig();

        try {
            pc.load(url);
        } catch (ConfigurationException e) {
            throw new ConfigException("Couldn't load url <" + url + ">");
        }

        configStack.push(pc);

        return this;
    }

    /**
     * Add config from an InputStream of UTF-8 properties data to the config stack.
     *
     * @param is input stream of UTF-8 properties data
     * @return this
     * @throws ConfigException if the input stream cannot be read
     */
    public ConfigModuleBuilder addPropertiesInputStream(@Nonnull InputStream is) throws ConfigException {
        PropertiesConfiguration pc = getNewPropertiesConfig();
        try {
            final BufferedInputStream bis = new BufferedInputStream(is);
            try {
                pc.load(bis);
            } finally {
                try {
                    bis.close();
                } catch (IOException e) {
                    logger.warn("Could not close input stream", e);
                }
            }
        } catch (ConfigurationException e) {
            throw new ConfigException("Couldn't load input stream", e);
        }

        configStack.push(pc);

        return this;
    }

    /**
     * Create a ConfigModule that will bind a ConfigurationObjectFactory backed by the config stack.
     *
     * @return a ConfigModule
     */
    public ConfigModule build() {

        CompositeConfiguration cc = new CompositeConfiguration();

        while (!configStack.isEmpty()) {
            cc.addConfiguration(configStack.pop());
        }

        return new ConfigModule(new ConfigurationObjectFactory(new CommonsConfigSource(cc)));
    }

    private PropertiesConfiguration getNewPropertiesConfig() {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setEncoding(StandardCharsets.UTF_8.name());
        pc.setDelimiterParsingDisabled(true);
        return pc;
    }
}
