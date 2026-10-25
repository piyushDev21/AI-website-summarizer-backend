package dev.piyush.researchassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        @DefaultValue("https://generativelanguage.googleapis.com/v1beta") String baseUrl,
        @DefaultValue("gemini-2.5-flash") String model) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
