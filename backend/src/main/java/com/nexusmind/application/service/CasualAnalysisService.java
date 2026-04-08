package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexusmind.application.ai.AiCompletionRequest;
import com.nexusmind.application.ai.AiProvider;
import com.nexusmind.application.ai.StructuredJsonExtractor;
import com.nexusmind.application.ai.TacticPromptCatalog;
import com.nexusmind.application.dto.CasualAnalysisRequestDto;
import com.nexusmind.domain.model.CasualAnalysisReport;
import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.infrastructure.persistence.CasualAnalysisReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CasualAnalysisService {

    private final AiProvider aiProvider;
    private final StructuredJsonExtractor jsonExtractor;
    private final AnalysisFallbackFactory fallbackFactory;
    private final GameCatalogService gameCatalogService;
    private final CasualAnalysisReportRepository casualAnalysisReportRepository;
    private final ReportSectionSyncService reportSectionSyncService;
    private final ObjectMapper objectMapper;

    public CasualAnalysisService(
            AiProvider aiProvider,
            StructuredJsonExtractor jsonExtractor,
            AnalysisFallbackFactory fallbackFactory,
            GameCatalogService gameCatalogService,
            CasualAnalysisReportRepository casualAnalysisReportRepository,
            ReportSectionSyncService reportSectionSyncService,
            ObjectMapper objectMapper
    ) {
        this.aiProvider = aiProvider;
        this.jsonExtractor = jsonExtractor;
        this.fallbackFactory = fallbackFactory;
        this.gameCatalogService = gameCatalogService;
        this.casualAnalysisReportRepository = casualAnalysisReportRepository;
        this.reportSectionSyncService = reportSectionSyncService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CasualAnalysisReport analyze(CasualAnalysisRequestDto req) {
        String dataContext = """
                Patch (campeões seed): %s
                Elo: %s | Lane: %s | Estilo: %s | Região: %s | Favorito: %s
                """.formatted(
                gameCatalogService.buildChampionContextSnippet(),
                req.elo(),
                req.lane(),
                req.playstyle(),
                req.region() != null ? req.region() : "n/d",
                req.favoriteChampionKey() != null ? req.favoriteChampionKey() : "n/d"
        );

        String userMessage = dataContext + "\n\n" + TacticPromptCatalog.casualJsonSchemaHint();

        String raw = aiProvider.complete(new AiCompletionRequest(
                TacticPromptCatalog.systemAnalyst(),
                userMessage,
                true
        ));

        JsonNode structured;
        String reason;
        if (raw.isBlank()) {
            structured = fallbackFactory.casualFallback(
                    "IA indisponível: resposta vazia (API key, limite ou 503 sobrecarga — tente gemini-2.0-flash ou aguarde)."
            );
            reason = "fallback_empty_ai";
        } else {
            Optional<JsonNode> parsed = jsonExtractor.extract(raw);
            if (parsed.isPresent()) {
                structured = mergeMeta(parsed.get(), objectMapper);
                reason = "ai_ok";
            } else {
                structured = fallbackFactory.casualFallback("Resposta IA não era JSON válido");
                reason = "fallback_parse";
            }
        }

        CasualAnalysisReport report = new CasualAnalysisReport();
        report.setElo(req.elo());
        report.setLane(req.lane());
        report.setPlaystyle(req.playstyle());
        report.setRegion(req.region());
        report.setFavoriteChampionKey(req.favoriteChampionKey());
        report.setStructuredPayload(structured);
        report.setSummaryText(extractSummary(structured, reason));
        casualAnalysisReportRepository.save(report);

        reportSectionSyncService.replaceSections(ReportKind.CASUAL, report.getId(), structured);
        return report;
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

    private static String extractSummary(JsonNode structured, String reason) {
        if (structured.has("lanePhase") && structured.get("lanePhase").isObject()) {
            JsonNode lp = structured.get("lanePhase");
            if (lp.has("levels1to3")) {
                String t = lp.get("levels1to3").asText("");
                return t.length() <= 280 ? t : t.substring(0, 280);
            }
        }
        return "Análise casual — " + reason;
    }
}
