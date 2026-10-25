package dev.piyush.researchassistant.research;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResearchRequest(
        @NotBlank @Size(max = ResearchRequest.MAX_CONTENT_LENGTH) String content,
        @NotNull Operation operation) {

    public static final int MAX_CONTENT_LENGTH = 100_000;
}
