package com.nexusmind.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.ReportSummaryDto;
import com.nexusmind.application.service.ReportDetailService;
import com.nexusmind.application.service.ReportListService;
import com.nexusmind.application.service.ReportPdfService;
import com.nexusmind.domain.model.CasualAnalysisReport;
import com.nexusmind.domain.model.DraftAnalysisReport;
import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.domain.model.ReportSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportListService reportListService;
    private final ReportDetailService reportDetailService;
    private final ReportPdfService reportPdfService;

    public ReportController(
            ReportListService reportListService,
            ReportDetailService reportDetailService,
            ReportPdfService reportPdfService
    ) {
        this.reportListService = reportListService;
        this.reportDetailService = reportDetailService;
        this.reportPdfService = reportPdfService;
    }

    @GetMapping
    public Page<ReportSummaryDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return reportListService.list(pageable);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(
            @PathVariable UUID id,
            @RequestParam("kind") String kind
    ) {
        JsonNode structured = reportDetailService.structuredPayload(kind, id);
        List<ReportSection> sections = reportDetailService.sections(
                "CASUAL".equalsIgnoreCase(kind) ? ReportKind.CASUAL : ReportKind.DRAFT,
                id
        );
        Map<String, Object> meta = new LinkedHashMap<>();
        if ("CASUAL".equalsIgnoreCase(kind)) {
            CasualAnalysisReport r = reportDetailService.getCasual(id);
            meta.put("elo", r.getElo());
            meta.put("lane", r.getLane());
            meta.put("playstyle", r.getPlaystyle());
            meta.put("createdAt", r.getCreatedAt().toString());
        } else {
            DraftAnalysisReport r = reportDetailService.getDraft(id);
            meta.put("side", r.getSide());
            meta.put("contextType", r.getContextType());
            meta.put("createdAt", r.getCreatedAt().toString());
        }
        return Map.of(
                "id", id.toString(),
                "kind", kind.toUpperCase(),
                "meta", meta,
                "structured", structured,
                "sections", sections.stream().map(s -> Map.of(
                        "key", s.getSectionKey(),
                        "title", s.getTitle() != null ? s.getTitle() : "",
                        "body", s.getBody()
                )).toList()
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(
            @PathVariable UUID id,
            @RequestParam("kind") String kind
    ) {
        byte[] bytes = reportPdfService.buildPdf(kind, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nexusmind-" + kind + "-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
