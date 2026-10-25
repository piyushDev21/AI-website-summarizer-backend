package dev.piyush.researchassistant;

import dev.piyush.researchassistant.config.GeminiProperties;
import dev.piyush.researchassistant.gemini.GeminiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(value = GeminiClient.class, properties = {"gemini.api-key=test-key", "gemini.model=test-model"})
@EnableConfigurationProperties(GeminiProperties.class)
class GeminiClientTest {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/test-model:generateContent";

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void sendsPromptWithKeyInHeaderAndReturnsText() {
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("Hello"))
                .andRespond(withSuccess("""
                        {"candidates": [{"content": {"parts": [{"text": "Hi "}, {"text": "there"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(geminiClient.generate("Hello")).isEqualTo("Hi there");
        server.verify();
    }

    @Test
    void reportsBlockedPrompts() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("""
                        {"promptFeedback": {"blockReason": "SAFETY"}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> geminiClient.generate("Hello"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.getReason()).contains("SAFETY");
                });
    }

    @Test
    void mapsRateLimitingTo429() {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> geminiClient.generate("Hello"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }
}
