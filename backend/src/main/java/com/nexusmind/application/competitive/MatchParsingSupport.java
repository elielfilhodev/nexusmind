package com.nexusmind.application.competitive;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.application.dto.competitive.MatchDtos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class MatchParsingSupport {

    private MatchParsingSupport() {
    }

    public static MatchDtos.MatchSummaryDto summarizeForPuuid(JsonNode match, String puuid) {
        JsonNode info = match.path("info");
        long gameCreation = info.path("gameCreation").asLong(0L);
        long duration = info.path("gameDuration").asLong(0L);
        int queueId = info.path("queueId").asInt(0);
        String seasonId = SeasonCatalog.seasonLabelForGameStart(Instant.ofEpochMilli(gameCreation));

        JsonNode participants = info.path("participants");
        JsonNode self = findParticipant(participants, puuid);
        if (self == null) {
            return null;
        }

        int champ = RiotJson.intVal(self, "championId");
        String lane = RiotJson.text(self, "lane");
        String role = RiotJson.text(self, "teamPosition");
        if (role.isBlank()) {
            role = RiotJson.text(self, "role");
        }
        boolean win = RiotJson.bool(self, "win");
        int kills = RiotJson.intVal(self, "kills");
        int deaths = RiotJson.intVal(self, "deaths");
        int assists = RiotJson.intVal(self, "assists");
        double kda = kda(kills, deaths, assists);
        int vision = RiotJson.intVal(self, "visionScore");
        int cs = RiotJson.intVal(self, "totalMinionsKilled") + RiotJson.intVal(self, "neutralMinionsKilled");
        double cpm = duration > 0 ? (cs * 60.0) / duration : 0.0;
        int teamId = RiotJson.intVal(self, "teamId");
        double kp = killParticipation(participants, teamId, puuid, kills, assists);

        List<Integer> items = new ArrayList<>(7);
        for (int i = 0; i <= 6; i++) {
            items.add(RiotJson.intVal(self, "item" + i));
        }
        List<Integer> spells = List.of(RiotJson.intVal(self, "summoner1Id"), RiotJson.intVal(self, "summoner2Id"));

        JsonNode perks = self.path("perks");
        int primaryStyle = perks.path("styles").isArray() && perks.path("styles").size() > 0
                ? perks.path("styles").get(0).path("style").asInt(0)
                : 0;
        int subStyle = perks.path("styles").isArray() && perks.path("styles").size() > 1
                ? perks.path("styles").get(1).path("style").asInt(0)
                : 0;
        List<Integer> perkSelections = extractPerkSelections(perks);

        boolean mvp = win && kda >= 4.0 && kills + assists >= 8;

        return new MatchDtos.MatchSummaryDto(
                match.path("metadata").path("matchId").asText(""),
                gameCreation,
                duration,
                queueId,
                QueueLabels.labelForQueueId(queueId),
                champ,
                lane,
                role,
                win,
                kills,
                deaths,
                assists,
                kda,
                vision,
                cs,
                cpm,
                kp,
                items,
                spells,
                primaryStyle,
                subStyle,
                perkSelections,
                mvp,
                seasonId
        );
    }

    public static MatchDtos.MatchDetailDto detailForPuuid(JsonNode match, String subjectPuuid) {
        JsonNode info = match.path("info");
        long gameCreation = info.path("gameCreation").asLong(0L);
        long duration = info.path("gameDuration").asLong(0L);
        int queueId = info.path("queueId").asInt(0);
        String gameVersion = info.path("gameVersion").asText("");
        JsonNode participants = info.path("participants");

        JsonNode self = findParticipant(participants, subjectPuuid);
        if (self == null) {
            return null;
        }
        int teamId = RiotJson.intVal(self, "teamId");
        List<MatchDtos.MatchParticipantDetailDto> allies = new ArrayList<>();
        List<MatchDtos.MatchParticipantDetailDto> enemies = new ArrayList<>();
        for (JsonNode p : participants) {
            if (subjectPuuid.equalsIgnoreCase(RiotJson.text(p, "puuid"))) {
                continue;
            }
            MatchDtos.MatchParticipantDetailDto row = mapParticipant(p, duration, participants);
            if (RiotJson.intVal(p, "teamId") == teamId) {
                allies.add(row);
            } else {
                enemies.add(row);
            }
        }
        MatchDtos.MatchParticipantDetailDto subject = mapParticipant(self, duration, participants);
        return new MatchDtos.MatchDetailDto(
                match.path("metadata").path("matchId").asText(""),
                gameCreation,
                duration,
                queueId,
                QueueLabels.labelForQueueId(queueId),
                gameVersion,
                subject,
                allies,
                enemies
        );
    }

    private static MatchDtos.MatchParticipantDetailDto mapParticipant(JsonNode p, long gameDurationSec, JsonNode allParticipants) {
        String puuid = RiotJson.text(p, "puuid");
        String riotId = RiotJson.text(p, "riotIdGameName") + "#" + RiotJson.text(p, "riotIdTagline");
        if (riotId.equals("#")) {
            riotId = RiotJson.text(p, "summonerName");
        }
        int kills = RiotJson.intVal(p, "kills");
        int deaths = RiotJson.intVal(p, "deaths");
        int assists = RiotJson.intVal(p, "assists");
        int cs = RiotJson.intVal(p, "totalMinionsKilled") + RiotJson.intVal(p, "neutralMinionsKilled");
        double cpm = gameDurationSec > 0 ? (cs * 60.0) / gameDurationSec : 0.0;
        int teamId = RiotJson.intVal(p, "teamId");
        double kp = killParticipation(allParticipants, teamId, puuid, kills, assists);

        List<Integer> items = new ArrayList<>(7);
        for (int i = 0; i <= 6; i++) {
            items.add(RiotJson.intVal(p, "item" + i));
        }
        List<Integer> spells = List.of(RiotJson.intVal(p, "summoner1Id"), RiotJson.intVal(p, "summoner2Id"));
        JsonNode perks = p.path("perks");
        int primaryStyle = perks.path("styles").isArray() && perks.path("styles").size() > 0
                ? perks.path("styles").get(0).path("style").asInt(0)
                : 0;
        int subStyle = perks.path("styles").isArray() && perks.path("styles").size() > 1
                ? perks.path("styles").get(1).path("style").asInt(0)
                : 0;
        List<Integer> perkSelections = extractPerkSelections(perks);

        String lane = RiotJson.text(p, "lane");
        String role = RiotJson.text(p, "teamPosition");
        if (role.isBlank()) {
            role = RiotJson.text(p, "role");
        }

        return new MatchDtos.MatchParticipantDetailDto(
                puuid,
                riotId,
                RiotJson.intVal(p, "championId"),
                lane,
                role,
                RiotJson.bool(p, "win"),
                teamId,
                kills,
                deaths,
                assists,
                RiotJson.intVal(p, "visionScore"),
                cs,
                cpm,
                kp,
                items,
                spells,
                primaryStyle,
                subStyle,
                perkSelections
        );
    }

    private static JsonNode findParticipant(JsonNode participants, String puuid) {
        if (!participants.isArray()) {
            return null;
        }
        for (JsonNode p : participants) {
            if (puuid.equalsIgnoreCase(RiotJson.text(p, "puuid"))) {
                return p;
            }
        }
        return null;
    }

    private static double kda(int k, int d, int a) {
        if (d <= 0) {
            return k + a;
        }
        return (k + a) / (double) d;
    }

    private static double killParticipation(JsonNode participants, int teamId, String puuid, int kills, int assists) {
        if (!participants.isArray()) {
            return 0.0;
        }
        int teamKills = 0;
        for (JsonNode p : participants) {
            if (RiotJson.intVal(p, "teamId") == teamId) {
                teamKills += RiotJson.intVal(p, "kills");
            }
        }
        if (teamKills <= 0) {
            return 0.0;
        }
        return 100.0 * (kills + assists) / teamKills;
    }

    private static List<Integer> extractPerkSelections(JsonNode perks) {
        List<Integer> ids = new ArrayList<>();
        JsonNode styles = perks.path("styles");
        if (!styles.isArray()) {
            return ids;
        }
        for (JsonNode style : styles) {
            JsonNode selections = style.path("selections");
            if (selections.isArray()) {
                for (JsonNode s : selections) {
                    ids.add(s.path("perk").asInt(0));
                }
            }
        }
        return ids;
    }
}
