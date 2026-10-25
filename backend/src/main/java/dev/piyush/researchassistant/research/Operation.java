package dev.piyush.researchassistant.research;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Operation {

    SUMMARIZE("summarize",
            "Write a clear, concise summary of the text below in one short paragraph of 3 to 5 sentences."),
    KEY_POINTS("key-points",
            "Extract the 5 to 8 most important points from the text below as a Markdown bullet list. "
                    + "Each bullet is one sentence and starts with the key idea in bold."),
    SUGGEST("suggest",
            "Based on the text below, suggest related topics worth researching next. Use a short Markdown "
                    + "heading per topic, followed by one sentence on why it is relevant.");

    private final String id;
    private final String instruction;

    Operation(String id, String instruction) {
        this.id = id;
        this.instruction = instruction;
    }

    public String instruction() {
        return instruction;
    }

    @JsonValue
    public String id() {
        return id;
    }

    @JsonCreator
    public static Operation fromId(String value) {
        return Arrays.stream(values())
                .filter(operation -> operation.id.equalsIgnoreCase(value) || operation.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown operation: " + value));
    }
}
