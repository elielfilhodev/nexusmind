package com.nexusmind.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.DraftAnalysisRequestDto;
import com.nexusmind.application.service.DraftAnalysisService;
import com.nexusmind.domain.model.DraftAnalysisReport;
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
public class DraftAnalysisController {

    private final DraftAnalysisService draftAnalysisService;

    public DraftAnalysisController(DraftAnalysisService draftAnalysisService) {
        this.draftAnalysisService = draftAnalysisService;
    }

    @PostMapping("/draft")
    public Map<String, Object> draft(@Valid @RequestBody DraftAnalysisRequestDto body) {
        DraftAnalysisReport r = draftAnalysisService.analyze(body);
        return toResponse(r.getId(), r.getStructuredPayload(), r.getSummaryText());
    }

    private static Map<String, Object> toResponse(UUID id, JsonNode payload, String summary) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.toString());
        m.put("kind", "DRAFT");
        m.put("summary", summary);
        m.put("structured", payload);
        return m;
    }
}
