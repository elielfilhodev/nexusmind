package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.infrastructure.riot.RiotApiClient;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Acesso à Riot com cache por entidade — reduz chamadas repetidas e protege quota.
 */
@Service
public class RiotCachedAccessor {

    private final RiotApiClient riotApiClient;

    public RiotCachedAccessor(RiotApiClient riotApiClient) {
        this.riotApiClient = riotApiClient;
    }

    @Cacheable(cacheNames = "riotSummoner", key = "#platform.name() + #puuid", unless = "#result == null || !#result.isPresent()")
    public Optional<JsonNode> summonerByPuuid(RiotPlatformId platform, String puuid) {
        return riotApiClient.getSummonerByPuuid(platform, puuid);
    }

    @Cacheable(cacheNames = "riotAccount", key = "#platform.name() + #puuid", unless = "#result == null || !#result.isPresent()")
    public Optional<JsonNode> accountByPuuid(RiotPlatformId platform, String puuid) {
        return riotApiClient.getAccountByPuuid(platform, puuid);
    }

    @Cacheable(cacheNames = "riotLeagueEntries", key = "#platform.name() + #puuid", unless = "#result == null || !#result.isPresent()")
    public Optional<JsonNode> leagueEntriesByPuuid(RiotPlatformId platform, String puuid) {
        return riotApiClient.getLeagueEntriesByPuuid(platform, puuid);
    }

    @Cacheable(cacheNames = "riotMastery", key = "#platform.name() + #puuid + ':' + #count", unless = "#result == null || !#result.isPresent()")
    public Optional<JsonNode> masteryTop(RiotPlatformId platform, String puuid, int count) {
        return riotApiClient.getChampionMasteriesTop(platform, puuid, count);
    }

    @Cacheable(
            cacheNames = "riotMatchIds",
            key = "#platform.name() + #puuid + ':' + #start + ':' + #count + ':' + (#queueId != null ? #queueId : 'all')",
            unless = "#result == null || !#result.isPresent()"
    )
    public Optional<JsonNode> matchIds(RiotPlatformId platform, String puuid, int start, int count, Integer queueId) {
        return riotApiClient.getMatchIds(platform, puuid, start, count, java.util.Optional.ofNullable(queueId));
    }

    @Cacheable(cacheNames = "riotMatchDetail", key = "#platform.name() + #matchId", unless = "#result == null || !#result.isPresent()")
    public Optional<JsonNode> match(RiotPlatformId platform, String matchId) {
        return riotApiClient.getMatch(platform, matchId);
    }
}
