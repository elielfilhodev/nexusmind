package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexusmind.application.ai.AiCompletionRequest;
import com.nexusmind.application.ai.AiProvider;
import com.nexusmind.application.ai.StructuredJsonExtractor;
import com.nexusmind.application.ai.TacticPromptCatalog;
import com.nexusmind.application.dto.DraftAnalysisRequestDto;
import com.nexusmind.domain.model.DraftAnalysisReport;
import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.infrastructure.persistence.DraftAnalysisReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DraftAnalysisService {

    private final AiProvider aiProvider;
    private final StructuredJsonExtractor jsonExtractor;
    private final AnalysisFallbackFactory fallbackFactory;
    private final GameCatalogService gameCatalogService;
    private final DraftAnalysisReportRepository draftAnalysisReportRepository;
    private final ReportSectionSyncService reportSectionSyncService;
    private final ObjectMapper objectMapper;

    public DraftAnalysisService(
            AiProvider aiProvider,
            StructuredJsonExtractor jsonExtractor,
            AnalysisFallbackFactory fallbackFactory,
            GameCatalogService gameCatalogService,
            DraftAnalysisReportRepository draftAnalysisReportRepository,
            ReportSectionSyncService reportSectionSyncService,
            ObjectMapper objectMapper
    ) {
        this.aiProvider = aiProvider;
        this.jsonExtractor = jsonExtractor;
        this.fallbackFactory = fallbackFactory;
        this.gameCatalogService = gameCatalogService;
        this.draftAnalysisReportRepository = draftAnalysisReportRepository;
        this.reportSectionSyncService = reportSectionSyncService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DraftAnalysisReport analyze(DraftAnalysisRequestDto req) {
        ObjectNode draftPayload = objectMapper.createObjectNode();
        draftPayload.put("side", req.side() != null ? req.side() : "UNKNOWN");
        draftPayload.set("ally", toTeam(req.ally()));
        draftPayload.set("enemy", toTeam(req.enemy()));
        draftPayload.put("contextType", req.contextType() != null ? req.contextType() : "GENERIC");
        draftPayload.put("strategicFocus", req.strategicFocus() != null ? req.strategicFocus() : "BALANCED");

        String dataContext = """
                Campeões conhecidos (seed): %s
                
                Draft JSON:
                %s
                """.formatted(
                gameCatalogService.buildChampionContextSnippet(),
                draftPayload.toPrettyString()
        );

        String userMessage = dataContext + "\n\n" + TacticPromptCatalog.draftJsonSchemaHint();

        String raw = aiProvider.complete(new AiCompletionRequest(
                TacticPromptCatalog.systemDraftAnalyst(),
                userMessage,
                true
        ));

        JsonNode structured;
        if (raw.isBlank()) {
            structured = fallbackFactory.draftFallback(
                    "IA indisponível: resposta vazia (API key, limite do modelo ou 503 sobrecarga — tente outro modelo, ex. gemini-2.0-flash, ou aguarde)."
            );
        } else {
            Optional<JsonNode> parsed = jsonExtractor.extract(raw);
            structured = parsed.map(n -> mergeMeta(n, objectMapper))
                    .orElseGet(() -> fallbackFactory.draftFallback("Resposta IA não era JSON válido"));
        }

        DraftAnalysisReport report = new DraftAnalysisReport();
        report.setSide(req.side());
        report.setContextType(req.contextType());
        report.setStrategicFocus(req.strategicFocus());
        report.setDraftPayload(draftPayload);
        report.setStructuredPayload(structured);
        report.setSummaryText(structured.path("executiveSummary").asText("Draft analysis"));
        draftAnalysisReportRepository.save(report);

        reportSectionSyncService.replaceSections(ReportKind.DRAFT, report.getId(), structured);
        return report;
    }

    private ObjectNode toTeam(com.nexusmind.application.dto.DraftSlotDto t) {
        ObjectNode n = objectMapper.createObjectNode();
        put(n, "TOP", t.top());
        put(n, "JUNGLE", t.jungle());
        put(n, "MID", t.mid());
        put(n, "ADC", t.adc());
        put(n, "SUPPORT", t.support());
        return n;
    }

    private static void put(ObjectNode n, String lane, String v) {
        if (v != null && !v.isBlank()) {
            n.put(lane, v);
        } else {
            n.putNull(lane);
        }
    }

    private static JsonNode mergeMeta(JsonNode aiNode, ObjectMapper mapper) {
        if (!aiNode.isObject()) {
            return aiNode;
        }
        ObjectNode copy = aiNode.deepCopy();
        if (!copy.has("meta") || !copy.get("meta").isObject()) {
            copy.set("meta", mapper.createObjectNode().put("status", "OK"));
        } else {
            ((ObjectNode) copy.get("meta")).put("status", "OK");
        }
        return copy;
    }
}
