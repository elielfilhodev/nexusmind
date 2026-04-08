package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.competitive.LeaderboardDtos;
import com.nexusmind.application.competitive.RiotJson;
import com.nexusmind.domain.model.ProPlayerEntity;
import com.nexusmind.infrastructure.persistence.ProPlayerRepository;
import com.nexusmind.infrastructure.riot.RiotApiClient;
import com.nexusmind.infrastructure.riot.RiotAssetResolver;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class RiotPlayerSearchService {

    private final RiotApiClient riotApiClient;
    private final RiotCachedAccessor cached;
    private final RiotAssetResolver assets;
    private final ProPlayerRepository proPlayerRepository;
    private final GameCatalogService gameCatalogService;

    public RiotPlayerSearchService(
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

    public LeaderboardDtos.PlayerSearchResultDto search(RiotPlatformId platform, String gameName, String tagLine) {
        if (!riotApiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "RIOT_API_KEY não configurada");
        }
        if (gameName == null || gameName.isBlank() || tagLine == null || tagLine.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameName e tagLine são obrigatórios");
        }
        Optional<JsonNode> acc = riotApiClient.getAccountByRiotId(platform, gameName.trim(), tagLine.trim());
        if (acc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada");
        }
        String puuid = RiotJson.text(acc.get(), "puuid");
        Optional<JsonNode> sum = cached.summonerByPuuid(platform, puuid);
        String level = sum.map(s -> String.valueOf(RiotJson.intVal(s, "summonerLevel"))).orElse("?");
        int icon = sum.map(s -> RiotJson.intVal(s, "profileIconId")).orElse(0);
        String ddragon = safePatch();
        String gn = RiotJson.text(acc.get(), "gameName");
        String tag = RiotJson.text(acc.get(), "tagLine");
        String riotId = gn + "#" + tag;
        boolean pro = proPlayerRepository.findByPuuidAndPlatformIdAndActiveIsTrue(puuid, platform.name()).isPresent();
        return new LeaderboardDtos.PlayerSearchResultDto(
                platform.name(),
                puuid,
                riotId,
                level,
                assets.profileIconUrl(ddragon, icon),
                pro,
                "/competitive/player/" + platform.name() + "/" + UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8)
        );
    }

    private String safePatch() {
        try {
            return gameCatalogService.getCurrentPatch().version();
        } catch (Exception e) {
            return "14.1.1";
        }
    }
}
