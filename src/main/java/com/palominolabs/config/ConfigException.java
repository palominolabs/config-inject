package com.palominolabs.config;

public final class ConfigException extends Exception {
    ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable e) {
        super(message, e);
    }
}
