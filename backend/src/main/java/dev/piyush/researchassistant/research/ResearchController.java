package dev.piyush.researchassistant.research;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchService researchService;

    @PostMapping("/process")
    public ResearchResponse process(@Valid @RequestBody ResearchRequest request) {
        return researchService.process(request);
    }
}
