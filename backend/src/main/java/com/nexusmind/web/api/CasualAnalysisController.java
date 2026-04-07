package com.nexusmind.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.CasualAnalysisRequestDto;
import com.nexusmind.application.service.CasualAnalysisService;
import com.nexusmind.domain.model.CasualAnalysisReport;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
public class CasualAnalysisController {

    private final CasualAnalysisService casualAnalysisService;

    public CasualAnalysisController(CasualAnalysisService casualAnalysisService) {
        this.casualAnalysisService = casualAnalysisService;
    }

    @PostMapping("/casual")
    public Map<String, Object> casual(@Valid @RequestBody CasualAnalysisRequestDto body) {
        CasualAnalysisReport r = casualAnalysisService.analyze(body);
        return toResponse(r.getId(), r.getStructuredPayload(), r.getSummaryText());
    }

    private static Map<String, Object> toResponse(UUID id, JsonNode payload, String summary) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.toString());
        m.put("kind", "CASUAL");
        m.put("summary", summary);
        m.put("structured", payload);
        return m;
    }
}
