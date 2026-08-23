package com.speed.engramstudio.domain.model;

public record EngramVersion(
    int major,
    int minor,
    int patch
) {
    public static EngramVersion parse(String versionString) {
        if (versionString == null || versionString.isBlank()) {
            return new EngramVersion(0, 0, 0);
        }
        
        String cleaned = versionString.startsWith("v") ? versionString.substring(1) : versionString;
        String[] parts = cleaned.split("\\.");
        
        int major = parts.length > 0 ? parsePart(parts[0]) : 0;
        int minor = parts.length > 1 ? parsePart(parts[1]) : 0;
        int patch = parts.length > 2 ? parsePart(parts[2]) : 0;
        
        return new EngramVersion(major, minor, patch);
    }
    
    private static int parsePart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}