package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmind.application.ai.AiCompletionRequest;
import com.nexusmind.application.ai.AiProvider;
import com.nexusmind.application.ai.StructuredJsonExtractor;
import com.nexusmind.application.dto.competitive.AiAnalysisDtos;
import com.nexusmind.application.dto.competitive.MatchDtos;
import com.nexusmind.application.dto.competitive.PlayerProfileDtos;
import com.nexusmind.application.competitive.MatchAnalysisAiPrompt;
import com.nexusmind.application.competitive.MatchParsingSupport;
import com.nexusmind.domain.model.CompetitiveAiCacheEntity;
import com.nexusmind.infrastructure.ai.AiProperties;
import com.nexusmind.infrastructure.persistence.CompetitiveAiCacheRepository;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompetitiveAiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CompetitiveAiAnalysisService.class);

    /** Versão do payload de análise de partida (bump ao mudar o schema do prompt). */
    private static final String KIND_MATCH = "MATCH_ANALYSIS_V2";
    private static final String KIND_PROFILE = "PROFILE_ANALYSIS";

    private final AiProvider aiProvider;
    private final AiProperties aiProperties;
    private final StructuredJsonExtractor jsonExtractor;
    private final ObjectMapper objectMapper;
    private final CompetitiveAiCacheRepository cacheRepository;
    private final RiotCachedAccessor cached;
    private final RiotPlayerProfileService playerProfileService;

    public CompetitiveAiAnalysisService(
            AiProvider aiProvider,
            AiProperties aiProperties,
            StructuredJsonExtractor jsonExtractor,
            ObjectMapper objectMapper,
            CompetitiveAiCacheRepository cacheRepository,
            RiotCachedAccessor cached,
            RiotPlayerProfileService playerProfileService
    ) {
        this.aiProvider = aiProvider;
        this.aiProperties = aiProperties;
        this.jsonExtractor = jsonExtractor;
        this.objectMapper = objectMapper;
        this.cacheRepository = cacheRepository;
        this.cached = cached;
        this.playerProfileService = playerProfileService;
    }

    @Transactional
    public AiAnalysisDtos.MatchAiAnalysisDto analyzeMatch(RiotPlatformId platform, String matchId, String puuid) {
        String key = platform.name() + ":" + matchId + ":" + puuid;
        Optional<CompetitiveAiCacheEntity> hit = cacheRepository.findByCacheKeyAndKind(key, KIND_MATCH);
        if (hit.isPresent()) {
            return parseMatch(hit.get().getPayloadJson(), matchId, puuid, hit.get().getModel());
        }
        JsonNode match = cached.match(platform, matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        MatchDtos.MatchSummaryDto sum = MatchParsingSupport.summarizeForPuuid(match, puuid);
        if (sum == null) {
            throw new IllegalArgumentException("Participante não encontrado na partida");
        }
        String user = buildMatchPrompt(sum, match);
        String raw = aiProvider.complete(new AiCompletionRequest(MatchAnalysisAiPrompt.SYSTEM, user, true));
        Optional<JsonNode> parsed = jsonExtractor.extract(raw);
        AiAnalysisDtos.MatchAiAnalysisDto dto = parsed
                .map(n -> mapMatch(n, matchId, puuid, aiProperties.model()))
                .orElseGet(() -> fallbackMatch(matchId, puuid));
        persist(key, KIND_MATCH, dto, aiProperties.model());
        return dto;
    }

    @Transactional
    public AiAnalysisDtos.ProfileAiAnalysisDto analyzeProfile(RiotPlatformId platform, String puuid) {
        String key = platform.name() + ":" + puuid;
        Optional<CompetitiveAiCacheEntity> hit = cacheRepository.findByCacheKeyAndKind(key, KIND_PROFILE);
        if (hit.isPresent()) {
            return parseProfile(hit.get().getPayloadJson(), puuid, hit.get().getModel());
        }
        PlayerProfileDtos.PlayerProfileDto profile = playerProfileService.profile(platform, puuid);
        String user;
        try {
            user = "Perfil agregado (JSON): " + objectMapper.writeValueAsString(profile);
        } catch (Exception e) {
            user = "Perfil indisponível para serialização";
        }
        String system = """
                Você é scout/coach de League of Legends. Responda em português do Brasil.
                Produza JSON estrito com campos:
                overview (parágrafo curto),
                traits (array de strings),
                risks (array de strings),
                recommendations (array de strings),
                competitiveRead (leitura competitiva em 2-4 frases).
                Sem markdown, sem texto fora do JSON.""";
        String raw = aiProvider.complete(new AiCompletionRequest(system, user, true));
        Optional<JsonNode> parsed = jsonExtractor.extract(raw);
        AiAnalysisDtos.ProfileAiAnalysisDto dto = parsed
                .map(n -> mapProfile(n, puuid, aiProperties.model()))
                .orElseGet(() -> fallbackProfile(puuid));
        persist(key, KIND_PROFILE, dto, aiProperties.model());
        return dto;
    }

    private void persist(String cacheKey, String kind, Object dto, String model) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            Optional<CompetitiveAiCacheEntity> existing = cacheRepository.findByCacheKeyAndKind(cacheKey, kind);
            if (existing.isPresent()) {
                CompetitiveAiCacheEntity e = existing.get();
                e.setPayloadJson(json);
                e.setModel(model);
                cacheRepository.save(e);
            } else {
                CompetitiveAiCacheEntity e = new CompetitiveAiCacheEntity();
                e.setCacheKey(cacheKey);
                e.setKind(kind);
                e.setPayloadJson(json);
                e.setModel(model);
                cacheRepository.save(e);
            }
        } catch (Exception ex) {
            log.warn("Falha ao persistir cache de análise competitiva: {}", ex.getMessage());
        }
    }

    private static String buildMatchPrompt(MatchDtos.MatchSummaryDto sum, JsonNode match) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resumo da partida do jogador focado:\n");
        sb.append("- Campeão id: ").append(sum.championId()).append("\n");
        sb.append("- Lane/role: ").append(sum.lane()).append(" / ").append(sum.role()).append("\n");
        sb.append("- Resultado: ").append(sum.win() ? "vitória" : "derrota").append("\n");
        sb.append("- KDA: ").append(sum.kills()).append("/").append(sum.deaths()).append("/").append(sum.assists())
                .append(" (KDA ").append(String.format("%.2f", sum.kda())).append(")\n");
        sb.append("- CS/min: ").append(String.format("%.2f", sum.csPerMin())).append("\n");
        sb.append("- Vision score: ").append(sum.visionScore()).append("\n");
        sb.append("- KP%: ").append(String.format("%.1f", sum.killParticipation())).append("\n");
        sb.append("- Fila: ").append(sum.queueLabel()).append("\n");
        sb.append("- Duração (s): ").append(sum.durationSec()).append("\n");
        sb.append("Itens finais (ids): ").append(sum.items()).append("\n");
        sb.append("Feitiços (ids): ").append(sum.summonerSpellIds()).append("\n");
        sb.append("Runas (perk ids aproximados): ").append(sum.perkSelections()).append("\n");
        sb.append("Versão do jogo: ").append(match.path("info").path("gameVersion").asText("")).append("\n");
        return sb.toString();
    }

    private AiAnalysisDtos.MatchAiAnalysisDto mapMatch(JsonNode n, String matchId, String puuid, String model) {
        AiAnalysisDtos.PerformanceRatingDto rating = performanceRating(n);
        List<String> playstyleRead = strings(n, "playstyleRead");
        if (playstyleRead.isEmpty() && n.has("playstyle")) {
            String ps = text(n, "playstyle");
            if (!ps.isBlank()) {
                playstyleRead = List.of(ps);
            }
        }
        List<String> improvement = strings(n, "improvementActions");
        if (improvement.isEmpty()) {
            improvement = strings(n, "tips");
        }
        return new AiAnalysisDtos.MatchAiAnalysisDto(
                matchId,
                puuid,
                text(n, "summary"),
                rating,
                strings(n, "strengths"),
                strings(n, "mistakes"),
                playstyleRead,
                text(n, "lanePhaseAssessment"),
                text(n, "midGameAssessment"),
                text(n, "lateGameAssessment"),
                text(n, "buildAssessment"),
                text(n, "runeAssessment"),
                text(n, "spellAssessment"),
                text(n, "macroAssessment"),
                strings(n, "consistencyNotes"),
                improvement,
                strings(n, "coachNotes"),
                model
        );
    }

    private static AiAnalysisDtos.PerformanceRatingDto performanceRating(JsonNode n) {
        JsonNode pr = n.path("performanceRating");
        if (pr.isMissingNode() || !pr.isObject()) {
            return new AiAnalysisDtos.PerformanceRatingDto(0, "");
        }
        int score = pr.path("score").asInt(0);
        String label = text(pr, "label");
        return new AiAnalysisDtos.PerformanceRatingDto(score, label);
    }

    private AiAnalysisDtos.ProfileAiAnalysisDto mapProfile(JsonNode n, String puuid, String model) {
        return new AiAnalysisDtos.ProfileAiAnalysisDto(
                puuid,
                text(n, "overview"),
                strings(n, "traits"),
                strings(n, "risks"),
                strings(n, "recommendations"),
                text(n, "competitiveRead"),
                model
        );
    }

    private AiAnalysisDtos.MatchAiAnalysisDto parseMatch(String json, String matchId, String puuid, String model) {
        try {
            JsonNode n = objectMapper.readTree(json);
            return mapMatch(n, matchId, puuid, model);
        } catch (Exception e) {
            return fallbackMatch(matchId, puuid);
        }
    }

    private AiAnalysisDtos.ProfileAiAnalysisDto parseProfile(String json, String puuid, String model) {
        try {
            JsonNode n = objectMapper.readTree(json);
            return mapProfile(n, puuid, model);
        } catch (Exception e) {
            return fallbackProfile(puuid);
        }
    }

    private AiAnalysisDtos.MatchAiAnalysisDto fallbackMatch(String matchId, String puuid) {
        return new AiAnalysisDtos.MatchAiAnalysisDto(
                matchId,
                puuid,
                "Análise indisponível — verifique AI_API_KEY ou tente novamente.",
                new AiAnalysisDtos.PerformanceRatingDto(0, "Indisponível"),
                List.of(),
                List.of(),
                List.of(),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                List.of("Sem resposta estruturada da IA ou formato JSON incompatível."),
                List.of(),
                List.of(),
                aiProperties.model()
        );
    }

    private AiAnalysisDtos.ProfileAiAnalysisDto fallbackProfile(String puuid) {
        return new AiAnalysisDtos.ProfileAiAnalysisDto(
                puuid,
                "Perfil não analisado — IA indisponível ou sem chave configurada.",
                List.of(),
                List.of(),
                List.of(),
                "Leitura indisponível.",
                aiProperties.model()
        );
    }

    private static String text(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return "";
        }
        return n.get(field).asText("");
    }

    private static List<String> strings(JsonNode n, String field) {
        if (n == null || !n.has(field) || !n.get(field).isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode x : n.get(field)) {
            out.add(x.asText(""));
        }
        return out;
    }
}
