package com.bence.projector.server.utils;

import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties instance;
    private final Properties properties;

    private AppProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException ignored) {
        }
    }

    public static AppProperties getInstance() {
        if (instance == null) {
            instance = new AppProperties();
        }
        return instance;
    }

    public String baseUrl() {
        return (String) properties.get("baseUrl");
    }
}
