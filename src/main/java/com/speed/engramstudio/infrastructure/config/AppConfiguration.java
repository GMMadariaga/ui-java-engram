package com.speed.engramstudio.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppConfiguration {
    
    private static final String CONFIG_FILE = "engram-studio.properties";
    private static final String SESSION_PREFIX = "agent.session.";
    private static final String DEFAULT_URL = "http://127.0.0.1:7437";
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final boolean DEFAULT_AUTO_START = true;
    
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
        properties.setProperty("engram.auto-start", String.valueOf(DEFAULT_AUTO_START));
        
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

    public boolean isAutoStart() {
        return Boolean.parseBoolean(properties.getProperty("engram.auto-start", String.valueOf(DEFAULT_AUTO_START)));
    }

    public void setAutoStart(boolean autoStart) {
        properties.setProperty("engram.auto-start", String.valueOf(autoStart));
    }

    public String getAgentExecutable(String agent) {
        return properties.getProperty("agent." + agent + ".executable", "");
    }

    public void setAgentExecutable(String agent, String executable) {
        String key = "agent." + agent + ".executable";
        if (executable == null || executable.isBlank()) {
            properties.remove(key);
        } else {
            properties.setProperty(key, executable.trim());
        }
    }
    
    public List<AgentSessionSetting> getAgentSessions() {
        List<AgentSessionSetting> sessions = new ArrayList<>();
        int count;
        try {
            count = Integer.parseInt(properties.getProperty(SESSION_PREFIX + "count", "0"));
        } catch (NumberFormatException e) {
            return sessions;
        }
        for (int index = 0; index < count; index++) {
            String prefix = SESSION_PREFIX + index + ".";
            String agentId = properties.getProperty(prefix + "agent-id", "");
            if (agentId.isBlank()) continue;
            sessions.add(new AgentSessionSetting(
                agentId,
                properties.getProperty(prefix + "agent-name", ""),
                properties.getProperty(prefix + "command", ""),
                properties.getProperty(prefix + "label", ""),
                properties.getProperty(prefix + "color", ""),
                Boolean.parseBoolean(properties.getProperty(prefix + "removable", "false"))));
        }
        return sessions;
    }

    public void saveAgentSessions(List<AgentSessionSetting> sessions) {
        // Reload first: other parts of the app keep their own instance and a
        // blind store() would revert whatever they saved after this one loaded.
        loadProperties();
        properties.keySet().removeIf(key -> key.toString().startsWith(SESSION_PREFIX));
        properties.setProperty(SESSION_PREFIX + "count", String.valueOf(sessions.size()));
        for (int index = 0; index < sessions.size(); index++) {
            AgentSessionSetting session = sessions.get(index);
            String prefix = SESSION_PREFIX + index + ".";
            properties.setProperty(prefix + "agent-id", session.agentId());
            properties.setProperty(prefix + "agent-name", session.agentName());
            properties.setProperty(prefix + "command", session.command());
            properties.setProperty(prefix + "label", session.label());
            properties.setProperty(prefix + "color", session.color());
            properties.setProperty(prefix + "removable", String.valueOf(session.removable()));
        }
        save();
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
