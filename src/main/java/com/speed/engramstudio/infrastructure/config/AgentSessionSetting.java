package com.speed.engramstudio.infrastructure.config;

/** Persisted identity and appearance of an agent terminal tab. */
public record AgentSessionSetting(String agentId, String agentName, String command,
                                  String label, String color, boolean removable) {
}
