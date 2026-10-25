package dev.piyush.researchassistant.research;

import dev.piyush.researchassistant.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResearchService {

    private final GeminiClient geminiClient;

    public ResearchResponse process(ResearchRequest request) {
        String result = geminiClient.generate(buildPrompt(request));
        return new ResearchResponse(request.operation(), result);
    }

    static String buildPrompt(ResearchRequest request) {
        return """
                You are a research assistant helping someone read a web page.
                %s
                Answer in the same language as the text. Do not add information that is not supported by it.

                Text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(request.operation().instruction(), request.content().strip());
    }
}
