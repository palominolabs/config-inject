package com.palominolabs.config;

/**
 * Generally failures in loading configuration are fatal, so this is an unchecked exception to make the common case less
 * verbose.
 */
public final class ConfigException extends RuntimeException {
    ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable e) {
        super(message, e);
    }
}
