config-inject
===========

[![Build Status](https://semaphoreci.com/api/v1/marshallpierce/config-inject/branches/master/shields_badge.svg)](https://semaphoreci.com/marshallpierce/config-inject)
 [ ![Download](https://api.bintray.com/packages/marshallpierce/maven/com.palominolabs.config%3Aconfig-inject/images/download.svg) ](https://bintray.com/marshallpierce/maven/com.palominolabs.config%3Aconfig-inject/_latestVersion) 

config-inject is a library to make it easy to inject configuration data into your classes with [Guice](http://code.google.com/p/google-guice/)
 and [config-magic](https://github.com/brianm/config-magic/).

Artifacts are published to [Bintray](https://bintray.com/marshallpierce/maven/com.palominolabs.config%3Aconfig-inject/view), so use `jcenter()` in your Gradle dependencies block (or analogous Maven config).

You can stack multiple sources of configuration data.

```java
ConfigModuleBuilder builder = new ConfigModuleBuilder();
// load from a file
builder.addPropertiesFile(new File("/foo/bar"))
        // or input stream
       .addPropertiesInputStream(getClass().getResourceAsStream("/some/other/config")
       // or system properties, or any other commons-configuration Configuration
       .addConfiguration(new SystemConfiguration());
```

Once you've set up your config data, use config-magic to access it. Define a config interface or abstract class:

```java
public interface SomeAppConfig {
    @Config("com.app.service.host")
    @Default("localhost")
    String getHost();

    @Config("com.app.service.port")
    @Default("1234")
    int getPort();
}
```

Bind up the necessary stuff in Guice:
```java
Guice.createInjector(builder.build(), new AbstractModule() {
        @Override
        protected void configure() {
            ConfigModule.bindConfigBean(binder(), SomeAppConfig.class);
        }
});
```

Now you can inject the config interface into your classes as needed.

```java
class SomeClass {
    @Inject
    SomeClass(SomeAppConfig config) {
        // do something configurable
    }
}
```
