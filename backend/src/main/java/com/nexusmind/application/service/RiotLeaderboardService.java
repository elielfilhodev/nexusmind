package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.competitive.LeaderboardDtos;
import com.nexusmind.application.competitive.RiotJson;
import com.nexusmind.domain.model.ProPlayerEntity;
import com.nexusmind.infrastructure.persistence.ProPlayerRepository;
import com.nexusmind.infrastructure.riot.RiotApiClient;
import com.nexusmind.infrastructure.riot.RiotAssetResolver;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RiotLeaderboardService {

    private final RiotApiClient riotApiClient;
    private final RiotCachedAccessor cached;
    private final RiotAssetResolver assets;
    private final ProPlayerRepository proPlayerRepository;
    private final GameCatalogService gameCatalogService;

    public RiotLeaderboardService(
            RiotApiClient riotApiClient,
            RiotCachedAccessor cached,
            RiotAssetResolver assets,
            ProPlayerRepository proPlayerRepository,
            GameCatalogService gameCatalogService
    ) {
        this.riotApiClient = riotApiClient;
        this.cached = cached;
        this.assets = assets;
        this.proPlayerRepository = proPlayerRepository;
        this.gameCatalogService = gameCatalogService;
    }

    public List<LeaderboardDtos.RegionOptionDto> regions() {
        List<LeaderboardDtos.RegionOptionDto> list = new ArrayList<>();
        for (RiotPlatformId p : RiotPlatformId.values()) {
            list.add(new LeaderboardDtos.RegionOptionDto(p.name(), p.shortLabel() + " (" + p.platformHost() + ")"));
        }
        return list;
    }

    @Cacheable(
            cacheNames = "leaderboardPage",
            key = "#platform.name() + ':' + #queue + ':' + #page + ':' + #size + ':' + #sort + ':' + #prosOnly",
            unless = "#result == null"
    )
    public LeaderboardDtos.LeaderboardPageDto leaderboard(
            RiotPlatformId platform,
            String queue,
            int page,
            int size,
            String sort,
            boolean prosOnly
    ) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        String q = normalizeQueue(queue);
        List<JsonNode> merged = mergeLeagues(platform, q);
        merged.sort(Comparator.comparingInt((JsonNode e) -> RiotJson.intVal(e, "leaguePoints")).reversed());

        List<Enriched> enriched = new ArrayList<>();
        int cap = Math.min(merged.size(), 500);
        for (int i = 0; i < cap; i++) {
            JsonNode e = merged.get(i);
            String puuid = resolvePuuid(platform, e);
            if (puuid.isBlank()) {
                continue;
            }
            int lp = RiotJson.intVal(e, "leaguePoints");
            int wins = RiotJson.intVal(e, "wins");
            int losses = RiotJson.intVal(e, "losses");
            double wr = (wins + losses) > 0 ? (100.0 * wins) / (wins + losses) : 0.0;
            String tier = textOr(e, "tier", "CHALLENGER");
            String rank = RiotJson.text(e, "rank");
            boolean pro = proPlayerRepository.findByPuuidAndPlatformIdAndActiveIsTrue(puuid, platform.name()).isPresent();
            enriched.add(new Enriched(puuid, lp, wins, losses, wr, tier, rank, pro));
        }

        if (prosOnly) {
            enriched = enriched.stream().filter(x -> x.professional).toList();
        }

        String sortKey = sort == null ? "LP_DESC" : sort.trim().toUpperCase();
        Comparator<Enriched> cmp = switch (sortKey) {
            case "WINRATE_DESC" -> Comparator.comparingDouble((Enriched x) -> x.winrate).reversed();
            default -> Comparator.comparingInt((Enriched x) -> x.leaguePoints).reversed();
        };
        enriched = new ArrayList<>(enriched);
        enriched.sort(cmp);

        long total = enriched.size();
        int from = Math.max(0, page * size);
        int to = Math.min(enriched.size(), from + size);
        List<Enriched> pageSlice = from < to ? enriched.subList(from, to) : List.of();

        String ddragon = safePatchVersion();

        List<LeaderboardDtos.LeaderboardRowDto> rows = new ArrayList<>();
        int position = from + 1;
        for (Enriched row : pageSlice) {
            Optional<JsonNode> acc = cached.accountByPuuid(platform, row.puuid);
            Optional<JsonNode> sum = cached.summonerByPuuid(platform, row.puuid);
            String game = acc.map(a -> RiotJson.text(a, "gameName")).orElse("");
            String tag = acc.map(a -> RiotJson.text(a, "tagLine")).orElse("");
            String riotId = (!game.isBlank() && !tag.isBlank()) ? (game + "#" + tag) : RiotJson.text(sum.orElse(null), "name");
            int icon = sum.map(s -> RiotJson.intVal(s, "profileIconId")).orElse(0);
            String iconUrl = assets.profileIconUrl(ddragon, icon);

            List<LeaderboardDtos.ChampionMasterySnippetDto> top = masterySnippets(platform, row.puuid);

            rows.add(new LeaderboardDtos.LeaderboardRowDto(
                    position++,
                    platform.name(),
                    row.puuid,
                    riotId,
                    RiotJson.text(sum.orElse(null), "name"),
                    row.tier,
                    row.rankDivision,
                    row.leaguePoints,
                    row.wins,
                    row.losses,
                    row.winrate,
                    iconUrl,
                    assets.rankedEmblemUrl(row.tier),
                    row.professional,
                    teamNameIfPro(platform, row.puuid),
                    top,
                    "/competitive/player/" + platform.name() + "/" + UriUtils.encodePathSegment(row.puuid, StandardCharsets.UTF_8)
            ));
        }

        return new LeaderboardDtos.LeaderboardPageDto(
                platform.name(),
                q,
                page,
                size,
                total,
                sortKey,
                rows
        );
    }

    private String teamNameIfPro(RiotPlatformId platform, String puuid) {
        return proPlayerRepository.findByPuuidAndPlatformIdAndActiveIsTrue(puuid, platform.name())
                .map(ProPlayerEntity::getTeamName)
                .orElse("");
    }

    private List<LeaderboardDtos.ChampionMasterySnippetDto> masterySnippets(RiotPlatformId platform, String puuid) {
        Optional<JsonNode> raw = cached.masteryTop(platform, puuid, 3);
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

    private String safePatchVersion() {
        try {
            return gameCatalogService.getCurrentPatch().version();
        } catch (Exception e) {
            return "14.1.1";
        }
    }

    private List<JsonNode> mergeLeagues(RiotPlatformId platform, String queue) {
        Optional<JsonNode> ch = riotApiClient.getChallengerLeague(platform, queue);
        Optional<JsonNode> gm = riotApiClient.getGrandmasterLeague(platform, queue);
        Map<String, JsonNode> byPuuid = new HashMap<>();
        ingest(ch, byPuuid);
        ingest(gm, byPuuid);
        return new ArrayList<>(byPuuid.values());
    }

    private static void ingest(Optional<JsonNode> league, Map<String, JsonNode> byPuuid) {
        if (league.isEmpty() || !league.get().has("entries")) {
            return;
        }
        for (JsonNode e : league.get().get("entries")) {
            String sid = RiotJson.text(e, "summonerId");
            String puuid = RiotJson.text(e, "puuid");
            String key = !puuid.isBlank() ? puuid : sid;
            if (key.isBlank()) {
                continue;
            }
            JsonNode prev = byPuuid.get(key);
            if (prev == null || RiotJson.intVal(e, "leaguePoints") > RiotJson.intVal(prev, "leaguePoints")) {
                byPuuid.put(key, e);
            }
        }
    }

    private String resolvePuuid(RiotPlatformId platform, JsonNode entry) {
        String puuid = RiotJson.text(entry, "puuid");
        if (!puuid.isBlank()) {
            return puuid;
        }
        String enc = RiotJson.text(entry, "summonerId");
        if (enc.isBlank()) {
            return "";
        }
        return riotApiClient.getSummonerByEncryptedId(platform, enc)
                .map(s -> RiotJson.text(s, "puuid"))
                .orElse("");
    }

    private static String textOr(JsonNode e, String field, String fallback) {
        String t = RiotJson.text(e, field);
        return t.isBlank() ? fallback : t;
    }

    private static String normalizeQueue(String queue) {
        if (queue == null || queue.isBlank()) {
            return "RANKED_SOLO_5x5";
        }
        String q = queue.trim();
        if ("RANKED_FLEX_SR".equalsIgnoreCase(q) || "FLEX".equalsIgnoreCase(q)) {
            return "RANKED_FLEX_SR";
        }
        return "RANKED_SOLO_5x5";
    }

    private record Enriched(
            String puuid,
            int leaguePoints,
            int wins,
            int losses,
            double winrate,
            String tier,
            String rankDivision,
            boolean professional
    ) {
    }
}
