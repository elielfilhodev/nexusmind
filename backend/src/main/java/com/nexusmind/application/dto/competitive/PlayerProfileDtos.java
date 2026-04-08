package com.nexusmind.application.dto.competitive;

import java.util.List;

public final class PlayerProfileDtos {

    private PlayerProfileDtos() {
    }

    public record PlayerProfileDto(
            String platformId,
            String puuid,
            String riotId,
            String summonerName,
            int summonerLevel,
            String profileIconUrl,
            RankInfoDto soloRank,
            RankInfoDto flexRank,
            String rankedEmblemUrl,
            boolean professional,
            String teamName,
            String primaryRole,
            String competitiveRegion,
            String scoutingTags,
            String bannerSplashUrl,
            int bannerChampionId,
            List<LeaderboardDtos.ChampionMasterySnippetDto> topMasteries,
            AggregatedStatsDto aggregated,
            List<SeasonSummaryDto> seasonSummaries,
            List<PeakRankDto> peakRanks,
            List<ChampionPlayDto> championPool,
            DataProvenanceDto provenance
    ) {
    }

    public record RankInfoDto(
            String queueType,
            String tier,
            String rank,
            int leaguePoints,
            int wins,
            int losses,
            boolean active
    ) {
    }

    public record AggregatedStatsDto(
            int totalGamesSample,
            double winrate,
            double avgKda,
            double avgCsPerMin,
            double avgVisionScore,
            double killParticipation
    ) {
    }

    public record SeasonSummaryDto(
            String seasonId,
            int games,
            int wins,
            double winrate,
            double avgKda
    ) {
    }

    public record PeakRankDto(
            String seasonId,
            String queueType,
            String tier,
            String rank,
            int leaguePoints,
            String note
    ) {
    }

    public record ChampionPlayDto(
            int championId,
            int games,
            int wins,
            double winrate,
            double avgKda,
            double avgCsPerMin
    ) {
    }

    public record DataProvenanceDto(
            String ddragonVersion,
            String disclaimer
    ) {
    }
}
