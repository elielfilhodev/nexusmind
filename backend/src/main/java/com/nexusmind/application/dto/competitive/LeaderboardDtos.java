package com.nexusmind.application.dto.competitive;

import java.util.List;

public final class LeaderboardDtos {

    private LeaderboardDtos() {
    }

    public record LeaderboardPageDto(
            String region,
            String queue,
            int page,
            int size,
            long total,
            String sort,
            List<LeaderboardRowDto> entries
    ) {
    }

    public record LeaderboardRowDto(
            int position,
            String platformId,
            String puuid,
            String riotId,
            String summonerName,
            String tier,
            String rankDivision,
            int leaguePoints,
            int wins,
            int losses,
            double winrate,
            String profileIconUrl,
            String rankedEmblemUrl,
            boolean professional,
            String teamName,
            List<ChampionMasterySnippetDto> topChampions,
            String profilePath
    ) {
    }

    public record ChampionMasterySnippetDto(
            int championId,
            long championPoints,
            int championLevel
    ) {
    }

    public record PlayerSearchResultDto(
            String platformId,
            String puuid,
            String riotId,
            String summonerLevel,
            String profileIconUrl,
            boolean professional,
            String profilePath
    ) {
    }

    public record RegionOptionDto(String platformId, String label) {
    }
}
