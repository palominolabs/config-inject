package com.palominolabs.config;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.HashMap;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class ConfigModuleTest {

    private ConfigModuleBuilder configModuleBuilder;

    @After
    public void tearDown() {
        System.getProperties().remove("conf1");
        System.getProperties().remove("conf2");
        System.getProperties().remove("conf3");
    }

    @Before
    public void setUp() throws Exception {
        configModuleBuilder = new ConfigModuleBuilder();
    }

    @Test
    public void testDefaults() {
        assertConfig("one", null, "three", getConfigBean());
    }

    @Test
    public void testPlainConfiguration() {
        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("conf1", "map-1");
        configModuleBuilder.addConfiguration(new MapConfiguration(configMap));

        assertConfig("map-1", null, "three", getConfigBean());
    }

    @Test
    public void testFile_Exists() throws ConfigException {
        configModuleBuilder.addPropertiesFile(new File("src/test/resources/conf1.properties"));

        assertConfig("file-1", null, "three", getConfigBean());
    }

    @Test
    public void testFile_DoesntExist() {

        try {
            configModuleBuilder.addPropertiesFile(new File("/foo/bar/baz"));
            fail();
        } catch (ConfigException e) {
            assertEquals("Couldn't load file </foo/bar/baz>", e.getMessage());
        }
    }

    @Test
    public void testUrl_Exists() throws MalformedURLException, ConfigException {
        configModuleBuilder.addPropertiesUrl(new URL("file:src/test/resources/conf1.properties"));

        assertConfig("file-1", null, "three", getConfigBean());
    }

    @Test
    public void testUrl_DoesntExist() throws MalformedURLException {
        try {
            configModuleBuilder.addPropertiesUrl(new URL("file://foo/bar/baz"));
            fail();
        } catch (ConfigException e) {
            assertEquals("Couldn't load url <file://foo/bar/baz>", e.getMessage());
        }
    }

    @Test
    public void testInputStream() throws ConfigException {
        configModuleBuilder.addPropertiesInputStream(ConfigModuleTest.class.getResourceAsStream("/conf1.properties"));

        assertConfig("file-1", null, "three", getConfigBean());
    }

    @Test
    public void testConfigStack() throws ConfigException {
        System.setProperty("conf1", "sys-1");
        System.setProperty("conf2", "sys-2");

        configModuleBuilder.addConfiguration(new SystemConfiguration())
            .addPropertiesInputStream(ConfigModuleTest.class.getResourceAsStream("/conf1.properties"));

        assertConfig("file-1", "sys-2", "three", getConfigBean());
    }

    @Test
    public void testGetBeanFromModule() {
        ConfigBean configBean = new ConfigModuleBuilder().build().getConfigBean(ConfigBean.class);
        assertConfig("one", null, "three", configBean);
    }

    private static void assertConfig(String one, String two, String three, ConfigBean configBean) {
        assertEquals(one, configBean.conf1());
        assertEquals(two, configBean.conf2());
        assertEquals(three, configBean.conf3());
    }

    static interface ConfigBean {

        @Config("conf1")
        @Default("one")
        String conf1();

        @Config("conf2")
        @DefaultNull
        String conf2();

        @Config("conf3")
        @Default("three")
        String conf3();
    }

    private ConfigBean getConfigBean() {
        Injector injector = Guice.createInjector(configModuleBuilder.build(), new BeanBinderModule());
        return injector.getInstance(ConfigBean.class);
    }

    private static class BeanBinderModule extends AbstractModule {
        @Override
        protected void configure() {
            ConfigModule.bindConfigBean(binder(), ConfigBean.class);
        }
    }
}
