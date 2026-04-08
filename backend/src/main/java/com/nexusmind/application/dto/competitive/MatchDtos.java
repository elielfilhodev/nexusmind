package com.nexusmind.application.dto.competitive;

import java.util.List;

public final class MatchDtos {

    private MatchDtos() {
    }

    public record MatchListDto(
            String platformId,
            String puuid,
            int page,
            int size,
            long total,
            List<MatchSummaryDto> matches
    ) {
    }

    public record MatchSummaryDto(
            String matchId,
            long gameCreation,
            long durationSec,
            int queueId,
            String queueLabel,
            int championId,
            String lane,
            String role,
            boolean win,
            int kills,
            int deaths,
            int assists,
            double kda,
            int visionScore,
            int cs,
            double csPerMin,
            double killParticipation,
            List<Integer> items,
            List<Integer> summonerSpellIds,
            int perkPrimaryStyle,
            int perkSubStyle,
            List<Integer> perkSelections,
            boolean mvpHeuristic,
            String seasonId
    ) {
    }

    public record MatchDetailDto(
            String matchId,
            long gameCreation,
            long durationSec,
            int queueId,
            String queueLabel,
            String gameVersion,
            MatchParticipantDetailDto subject,
            List<MatchParticipantDetailDto> sameTeam,
            List<MatchParticipantDetailDto> enemyTeam
    ) {
    }

    public record MatchParticipantDetailDto(
            String puuid,
            String riotId,
            int championId,
            String lane,
            String role,
            boolean win,
            int teamId,
            int kills,
            int deaths,
            int assists,
            int visionScore,
            int cs,
            double csPerMin,
            double killParticipation,
            List<Integer> items,
            List<Integer> summonerSpellIds,
            int perkPrimaryStyle,
            int perkSubStyle,
            List<Integer> perkSelections
    ) {
    }
}
