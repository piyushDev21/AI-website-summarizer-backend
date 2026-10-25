package dev.piyush.researchassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "gemini.api-key=")
@AutoConfigureMockMvc
class ResearchAssistantApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void explainsMissingApiKey() throws Exception {
        mockMvc.perform(post("/api/research/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Some article text", "operation": "summarize"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(
                        "Gemini API key is not configured. Set the GEMINI_API_KEY environment variable."));
    }
}
