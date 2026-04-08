package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.competitive.MatchParsingSupport;
import com.nexusmind.application.dto.competitive.MatchDtos;
import com.nexusmind.infrastructure.riot.RiotApiClient;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Objects;

@Service
public class RiotMatchAggregationService {

    private static final int FETCH_CAP = 100;

    private final RiotApiClient riotApiClient;
    private final RiotCachedAccessor cached;

    public RiotMatchAggregationService(RiotApiClient riotApiClient, RiotCachedAccessor cached) {
        this.riotApiClient = riotApiClient;
        this.cached = cached;
    }

    public MatchDtos.MatchListDto listMatches(
            RiotPlatformId platform,
            String puuid,
            int page,
            int size,
            String seasonId,
            Integer championId,
            Integer queueId,
            String outcome
    ) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        Integer queueFilter = queueId;
        OptionalJsonIds ids = fetchAllIds(platform, puuid, queueFilter);
        if (ids.ids.isEmpty()) {
            return new MatchDtos.MatchListDto(platform.name(), puuid, page, size, 0, List.of());
        }

        List<MatchDtos.MatchSummaryDto> all = new ArrayList<>();
        for (String matchId : ids.ids) {
            Optional<JsonNode> m = cached.match(platform, matchId);
            if (m.isEmpty()) {
                continue;
            }
            MatchDtos.MatchSummaryDto row = MatchParsingSupport.summarizeForPuuid(m.get(), puuid);
            if (row == null) {
                continue;
            }
            if (seasonId != null && !seasonId.isBlank() && !row.seasonId().equalsIgnoreCase(seasonId.trim())) {
                continue;
            }
            if (championId != null && row.championId() != championId) {
                continue;
            }
            if (queueId != null && row.queueId() != queueId) {
                continue;
            }
            if (outcome != null && !outcome.isBlank()) {
                String o = outcome.trim().toLowerCase(Locale.ROOT);
                if ("win".equals(o) && !row.win()) {
                    continue;
                }
                if ("loss".equals(o) && row.win()) {
                    continue;
                }
            }
            all.add(row);
        }

        long total = all.size();
        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);
        List<MatchDtos.MatchSummaryDto> slice = from < to ? all.subList(from, to) : List.of();
        return new MatchDtos.MatchListDto(platform.name(), puuid, page, size, total, slice);
    }

    /**
     * Amostra recente para agregações de perfil (sem filtros adicionais).
     */
    public List<MatchDtos.MatchSummaryDto> recentSummaries(RiotPlatformId platform, String puuid, int maxMatches, Integer queueId) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        int cap = Math.min(FETCH_CAP, Math.max(10, maxMatches));
        OptionalJsonIds ids = fetchAllIds(platform, puuid, queueId);
        List<MatchDtos.MatchSummaryDto> out = new ArrayList<>();
        for (String matchId : ids.ids) {
            if (out.size() >= cap) {
                break;
            }
            Optional<JsonNode> m = cached.match(platform, matchId);
            if (m.isEmpty()) {
                continue;
            }
            MatchDtos.MatchSummaryDto row = MatchParsingSupport.summarizeForPuuid(m.get(), puuid);
            if (row != null) {
                out.add(row);
            }
        }
        return out;
    }

    public MatchDtos.MatchDetailDto matchDetail(RiotPlatformId platform, String matchId, String puuid) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        JsonNode m = cached.match(platform, matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));
        MatchDtos.MatchDetailDto dto = MatchParsingSupport.detailForPuuid(m, puuid);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participante não encontrado na partida");
        }
        return dto;
    }

    private OptionalJsonIds fetchAllIds(RiotPlatformId platform, String puuid, Integer queueId) {
        List<String> out = new ArrayList<>();
        int start = 0;
        while (out.size() < FETCH_CAP) {
            Optional<JsonNode> chunk = cached.matchIds(platform, puuid, start, 20, queueId);
            if (chunk.isEmpty() || !chunk.get().isArray() || chunk.get().isEmpty()) {
                break;
            }
            for (JsonNode id : chunk.get()) {
                out.add(id.asText());
            }
            if (chunk.get().size() < 20) {
                break;
            }
            start += 20;
        }
        return new OptionalJsonIds(out);
    }

    private record OptionalJsonIds(List<String> ids) {
    }
}
