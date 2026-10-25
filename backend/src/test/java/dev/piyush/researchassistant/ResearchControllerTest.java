package dev.piyush.researchassistant;

import dev.piyush.researchassistant.research.Operation;
import dev.piyush.researchassistant.research.ResearchController;
import dev.piyush.researchassistant.research.ResearchResponse;
import dev.piyush.researchassistant.research.ResearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResearchController.class)
class ResearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResearchService researchService;

    @Test
    void processesContent() throws Exception {
        given(researchService.process(any())).willReturn(new ResearchResponse(Operation.KEY_POINTS, "- **Point**"));

        mockMvc.perform(post("/api/research/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Some article text", "operation": "key-points"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("key-points"))
                .andExpect(jsonPath("$.result").value("- **Point**"));
    }

    @Test
    void acceptsLegacyOperationNames() throws Exception {
        given(researchService.process(any())).willReturn(new ResearchResponse(Operation.SUMMARIZE, "Summary"));

        mockMvc.perform(post("/api/research/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Some article text", "operation": "SUMMARIZE"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/research/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "  ", "operation": "summarize"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownOperation() throws Exception {
        mockMvc.perform(post("/api/research/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "text", "operation": "translate"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allowsCallsFromChromeExtensions() throws Exception {
        mockMvc.perform(options("/api/research/process")
                        .header(HttpHeaders.ORIGIN, "chrome-extension://abcdefghijklmnop")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "chrome-extension://abcdefghijklmnop"));
    }

    @Test
    void blocksCallsFromOtherWebsites() throws Exception {
        mockMvc.perform(options("/api/research/process")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
