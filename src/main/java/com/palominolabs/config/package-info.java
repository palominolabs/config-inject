/**
 * <p></p>A mixture of <a href="http://code.google.com/p/google-guice/">Guice</a> and
 * <a href="https://github.com/brianm/config-magic/">config-magic</a> to make configuration a little easier.</p>
 *
 * <p></p>Use a {@code ConfigModuleBuilder} to define a stack of configuration sources, then include the resulting
 * {@code ConfigModule} in your Guice Injector creation. This binds a ConfigurationObjectFactory, which you can use
 * directly if you wish, but the common case would be to use {@code ConfigModule#bindConfigBean()} from other Guice
 * Modules to bind your own configuration beans.</p>
 */
package com.palominolabs.config;

