package dev.piyush.researchassistant.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.piyush.researchassistant.config.GeminiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thin client for the Gemini {@code generateContent} endpoint. The API key travels in the
 * {@code x-goog-api-key} header so it never appears in URLs or access logs.
 */
@Component
@Slf4j
public class GeminiClient {

    private final GeminiProperties properties;
    private final RestClient restClient;

    public GeminiClient(GeminiProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public String generate(String prompt) {
        if (!properties.hasApiKey()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini API key is not configured. Set the GEMINI_API_KEY environment variable.");
        }

        GenerateResponse response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent", properties.model())
                    .header("x-goog-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GenerateRequest.of(prompt))
                    .retrieve()
                    .body(GenerateResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("Gemini returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            HttpStatus status = e.getStatusCode().value() == 429 ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
            throw new ResponseStatusException(status, "Gemini request failed with status " + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.warn("Gemini is unreachable", e);
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Gemini did not respond in time");
        }

        return extractText(response);
    }

    private static String extractText(GenerateResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            String reason = response != null && response.promptFeedback() != null
                    ? response.promptFeedback().blockReason()
                    : null;
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    reason != null ? "Gemini blocked the request: " + reason : "Gemini returned no answer");
        }

        Content content = response.candidates().get(0).content();
        String text = content == null || content.parts() == null ? "" : content.parts().stream()
                .map(Part::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());

        if (text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty answer");
        }
        return text.strip();
    }

    record GenerateRequest(List<Content> contents) {
        static GenerateRequest of(String prompt) {
            return new GenerateRequest(List.of(new Content(List.of(new Part(prompt)))));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateResponse(List<Candidate> candidates, PromptFeedback promptFeedback) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PromptFeedback(String blockReason) {
    }
}
