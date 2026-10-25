package dev.piyush.researchassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResearchAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchAssistantApplication.class, args);
    }
}
