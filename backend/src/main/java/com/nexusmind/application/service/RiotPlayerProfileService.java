package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.competitive.LeaderboardDtos;
import com.nexusmind.application.dto.competitive.MatchDtos;
import com.nexusmind.application.dto.competitive.PlayerProfileDtos;
import com.nexusmind.application.competitive.RiotJson;
import com.nexusmind.domain.model.ProPlayerEntity;
import com.nexusmind.infrastructure.datadragon.DataDragonRemoteCatalog;
import com.nexusmind.infrastructure.persistence.ProPlayerRepository;
import com.nexusmind.infrastructure.riot.RiotApiClient;
import com.nexusmind.infrastructure.riot.RiotAssetResolver;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RiotPlayerProfileService {

    private final RiotApiClient riotApiClient;
    private final RiotCachedAccessor cached;
    private final RiotAssetResolver assets;
    private final ProPlayerRepository proPlayerRepository;
    private final GameCatalogService gameCatalogService;
    private final RiotMatchAggregationService matchAggregationService;
    private final DataDragonRemoteCatalog dataDragonRemoteCatalog;

    public RiotPlayerProfileService(
            RiotApiClient riotApiClient,
            RiotCachedAccessor cached,
            RiotAssetResolver assets,
            ProPlayerRepository proPlayerRepository,
            GameCatalogService gameCatalogService,
            RiotMatchAggregationService matchAggregationService,
            DataDragonRemoteCatalog dataDragonRemoteCatalog
    ) {
        this.riotApiClient = riotApiClient;
        this.cached = cached;
        this.assets = assets;
        this.proPlayerRepository = proPlayerRepository;
        this.gameCatalogService = gameCatalogService;
        this.matchAggregationService = matchAggregationService;
        this.dataDragonRemoteCatalog = dataDragonRemoteCatalog;
    }

    @Cacheable(cacheNames = "playerProfile", key = "#platform.name() + #puuid", unless = "#result == null")
    public PlayerProfileDtos.PlayerProfileDto profile(RiotPlatformId platform, String puuid) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        Optional<JsonNode> acc = cached.accountByPuuid(platform, puuid);
        Optional<JsonNode> sum = cached.summonerByPuuid(platform, puuid);
        if (sum.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invocador não encontrado");
        }
        String gameName = acc.map(a -> RiotJson.text(a, "gameName")).orElse(RiotJson.text(sum.get(), "name"));
        String tagLine = acc.map(a -> RiotJson.text(a, "tagLine")).orElse("");
        String riotId = (!gameName.isBlank() && !tagLine.isBlank()) ? (gameName + "#" + tagLine) : RiotJson.text(sum.get(), "name");

        Optional<JsonNode> leagues = cached.leagueEntriesByPuuid(platform, puuid);
        PlayerProfileDtos.RankInfoDto solo = null;
        PlayerProfileDtos.RankInfoDto flex = null;
        if (leagues.isPresent() && leagues.get().isArray()) {
            for (JsonNode e : leagues.get()) {
                String q = RiotJson.text(e, "queueType");
                if ("RANKED_SOLO_5x5".equals(q)) {
                    solo = mapRank(e);
                } else if ("RANKED_FLEX_SR".equals(q)) {
                    flex = mapRank(e);
                }
            }
        }

        String ddragon = safePatch();
        int icon = RiotJson.intVal(sum.get(), "profileIconId");
        String iconUrl = assets.profileIconUrl(ddragon, icon);
        String emblem = assets.rankedEmblemUrl(solo != null ? solo.tier() : "GOLD");

        Optional<ProPlayerEntity> pro = proPlayerRepository.findByPuuidAndPlatformIdAndActiveIsTrue(puuid, platform.name());
        boolean isPro = pro.isPresent();

        List<LeaderboardDtos.ChampionMasterySnippetDto> masteries = masteryList(platform, puuid, 10);

        int bannerChampionId = masteries.isEmpty() ? 0 : masteries.getFirst().championId();
        String bannerUrl = "";
        if (bannerChampionId > 0) {
            bannerUrl = dataDragonRemoteCatalog.resolveChampionKeyByNumericId(ddragon, bannerChampionId)
                    .map(k -> assets.championSplashUrl(ddragon, k))
                    .orElse("");
        }

        List<MatchDtos.MatchSummaryDto> sample = matchAggregationService.recentSummaries(platform, puuid, 40, null);
        PlayerProfileDtos.AggregatedStatsDto agg = aggregate(sample);
        List<PlayerProfileDtos.SeasonSummaryDto> seasons = seasonAgg(sample);
        List<PlayerProfileDtos.ChampionPlayDto> pool = championPool(sample);
        List<PlayerProfileDtos.PeakRankDto> peaks = peakSnapshot(solo, flex);

        String lane = inferLane(sample);
        String team = pro.map(ProPlayerEntity::getTeamName).orElse("");
        String role = pro.map(ProPlayerEntity::getPrimaryRole).orElse(lane);
        String compRegion = pro.map(ProPlayerEntity::getCompetitiveRegion).orElse("");
        String tags = pro.map(ProPlayerEntity::getScoutingTags).orElse("");

        String disclaimer = "Estatísticas agregadas a partir de partidas recentes via Match-V5. "
                + "Peak histórico completo não é exposto pela Riot API; valores de pico listados refletem snapshot atual da liga.";

        return new PlayerProfileDtos.PlayerProfileDto(
                platform.name(),
                puuid,
                riotId,
                RiotJson.text(sum.get(), "name"),
                RiotJson.intVal(sum.get(), "summonerLevel"),
                iconUrl,
                solo,
                flex,
                emblem,
                isPro,
                team,
                role,
                compRegion,
                tags,
                bannerUrl,
                bannerChampionId,
                masteries,
                agg,
                seasons,
                peaks,
                pool,
                new PlayerProfileDtos.DataProvenanceDto(ddragon, disclaimer)
        );
    }

    public List<String> seasonsForPlayer(RiotPlatformId platform, String puuid) {
        List<MatchDtos.MatchSummaryDto> sample = matchAggregationService.recentSummaries(platform, puuid, 80, null);
        return sample.stream()
                .map(MatchDtos.MatchSummaryDto::seasonId)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private List<LeaderboardDtos.ChampionMasterySnippetDto> masteryList(RiotPlatformId platform, String puuid, int count) {
        Optional<JsonNode> raw = cached.masteryTop(platform, puuid, count);
        if (raw.isEmpty() || !raw.get().isArray()) {
            return List.of();
        }
        List<LeaderboardDtos.ChampionMasterySnippetDto> list = new ArrayList<>();
        for (JsonNode m : raw.get()) {
            list.add(new LeaderboardDtos.ChampionMasterySnippetDto(
                    RiotJson.intVal(m, "championId"),
                    RiotJson.longVal(m, "championPoints"),
                    RiotJson.intVal(m, "championLevel")
            ));
        }
        return list;
    }

    private static PlayerProfileDtos.RankInfoDto mapRank(JsonNode e) {
        return new PlayerProfileDtos.RankInfoDto(
                RiotJson.text(e, "queueType"),
                RiotJson.text(e, "tier"),
                RiotJson.text(e, "rank"),
                RiotJson.intVal(e, "leaguePoints"),
                RiotJson.intVal(e, "wins"),
                RiotJson.intVal(e, "losses"),
                true
        );
    }

    private PlayerProfileDtos.AggregatedStatsDto aggregate(List<MatchDtos.MatchSummaryDto> rows) {
        if (rows.isEmpty()) {
            return new PlayerProfileDtos.AggregatedStatsDto(0, 0, 0, 0, 0, 0);
        }
        int n = rows.size();
        int wins = (int) rows.stream().filter(MatchDtos.MatchSummaryDto::win).count();
        double wr = 100.0 * wins / n;
        double kda = rows.stream().mapToDouble(MatchDtos.MatchSummaryDto::kda).average().orElse(0);
        double cs = rows.stream().mapToDouble(MatchDtos.MatchSummaryDto::csPerMin).average().orElse(0);
        double vis = rows.stream().mapToDouble(MatchDtos.MatchSummaryDto::visionScore).average().orElse(0);
        double kp = rows.stream().mapToDouble(MatchDtos.MatchSummaryDto::killParticipation).average().orElse(0);
        return new PlayerProfileDtos.AggregatedStatsDto(n, wr, kda, cs, vis, kp);
    }

    private List<PlayerProfileDtos.SeasonSummaryDto> seasonAgg(List<MatchDtos.MatchSummaryDto> rows) {
        Map<String, List<MatchDtos.MatchSummaryDto>> by = rows.stream().collect(Collectors.groupingBy(MatchDtos.MatchSummaryDto::seasonId));
        List<PlayerProfileDtos.SeasonSummaryDto> out = new ArrayList<>();
        for (Map.Entry<String, List<MatchDtos.MatchSummaryDto>> e : by.entrySet()) {
            List<MatchDtos.MatchSummaryDto> g = e.getValue();
            int games = g.size();
            int wins = (int) g.stream().filter(MatchDtos.MatchSummaryDto::win).count();
            double wr = games > 0 ? 100.0 * wins / games : 0;
            double kda = g.stream().mapToDouble(MatchDtos.MatchSummaryDto::kda).average().orElse(0);
            out.add(new PlayerProfileDtos.SeasonSummaryDto(e.getKey(), games, wins, wr, kda));
        }
        out.sort(Comparator.comparing(PlayerProfileDtos.SeasonSummaryDto::seasonId).reversed());
        return out;
    }

    private List<PlayerProfileDtos.ChampionPlayDto> championPool(List<MatchDtos.MatchSummaryDto> rows) {
        Map<Integer, List<MatchDtos.MatchSummaryDto>> by = rows.stream().collect(Collectors.groupingBy(MatchDtos.MatchSummaryDto::championId));
        Map<Integer, PlayerProfileDtos.ChampionPlayDto> scored = new HashMap<>();
        for (Map.Entry<Integer, List<MatchDtos.MatchSummaryDto>> e : by.entrySet()) {
            List<MatchDtos.MatchSummaryDto> g = e.getValue();
            int games = g.size();
            int wins = (int) g.stream().filter(MatchDtos.MatchSummaryDto::win).count();
            double wr = games > 0 ? 100.0 * wins / games : 0;
            double kda = g.stream().mapToDouble(MatchDtos.MatchSummaryDto::kda).average().orElse(0);
            double cpm = g.stream().mapToDouble(MatchDtos.MatchSummaryDto::csPerMin).average().orElse(0);
            scored.put(e.getKey(), new PlayerProfileDtos.ChampionPlayDto(e.getKey(), games, wins, wr, kda, cpm));
        }
        return scored.values().stream()
                .sorted(Comparator.comparingInt(PlayerProfileDtos.ChampionPlayDto::games).reversed())
                .limit(12)
                .collect(Collectors.toList());
    }

    private List<PlayerProfileDtos.PeakRankDto> peakSnapshot(PlayerProfileDtos.RankInfoDto solo, PlayerProfileDtos.RankInfoDto flex) {
        List<PlayerProfileDtos.PeakRankDto> list = new ArrayList<>();
        if (solo != null) {
            list.add(new PlayerProfileDtos.PeakRankDto(
                    "CURRENT",
                    solo.queueType(),
                    solo.tier(),
                    solo.rank(),
                    solo.leaguePoints(),
                    "Snapshot atual (League API)"
            ));
        }
        if (flex != null) {
            list.add(new PlayerProfileDtos.PeakRankDto(
                    "CURRENT",
                    flex.queueType(),
                    flex.tier(),
                    flex.rank(),
                    flex.leaguePoints(),
                    "Snapshot atual (League API)"
            ));
        }
        return list;
    }

    private String inferLane(List<MatchDtos.MatchSummaryDto> rows) {
        Map<String, Long> lanes = new LinkedHashMap<>();
        for (MatchDtos.MatchSummaryDto m : rows) {
            String lane = m.role();
            if (lane == null || lane.isBlank()) {
                lane = m.lane();
            }
            if (lane == null || lane.isBlank() || "NONE".equalsIgnoreCase(lane)) {
                continue;
            }
            String key = lane.toUpperCase(Locale.ROOT);
            lanes.merge(key, 1L, Long::sum);
        }
        return lanes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private String safePatch() {
        try {
            return gameCatalogService.getCurrentPatch().version();
        } catch (Exception e) {
            return "14.1.1";
        }
    }
}
