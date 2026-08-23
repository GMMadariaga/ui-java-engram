package com.speed.engramstudio.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppConfiguration {
    
    private static final String CONFIG_FILE = "engram-studio.properties";
    private static final String DEFAULT_URL = "http://127.0.0.1:7437";
    private static final int DEFAULT_TIMEOUT = 5000;
    
    private final Properties properties;
    private final Path configPath;
    
    public AppConfiguration() {
        this.properties = new Properties();
        this.configPath = getConfigPath();
        loadProperties();
    }
    
    private Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".engram-studio", CONFIG_FILE);
    }
    
    private void loadProperties() {
        // Load defaults
        properties.setProperty("engram.url", DEFAULT_URL);
        properties.setProperty("engram.timeout", String.valueOf(DEFAULT_TIMEOUT));
        properties.setProperty("engram.auto-connect", "true");
        
        // Load from file if exists
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not load config file: " + e.getMessage());
            }
        }
    }
    
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (var output = Files.newOutputStream(configPath)) {
                properties.store(output, "Engram Studio Configuration");
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not save config file: " + e.getMessage());
        }
    }
    
    public String getEngramUrl() {
        return properties.getProperty("engram.url", DEFAULT_URL);
    }
    
    public void setEngramUrl(String url) {
        properties.setProperty("engram.url", url);
    }
    
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("engram.timeout", String.valueOf(DEFAULT_TIMEOUT)));
    }
    
    public void setTimeout(int timeout) {
        properties.setProperty("engram.timeout", String.valueOf(timeout));
    }
    
    public boolean isAutoConnect() {
        return Boolean.parseBoolean(properties.getProperty("engram.auto-connect", "true"));
    }
    
    public void setAutoConnect(boolean autoConnect) {
        properties.setProperty("engram.auto-connect", String.valueOf(autoConnect));
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}