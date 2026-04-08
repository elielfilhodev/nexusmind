package com.nexusmind.infrastructure.riot;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Plataform routing hosts da Riot (LoL). O par regionalRouting é usado pela Account API v1.
 */
public enum RiotPlatformId {
    BR1("br1", "americas", "BR"),
    LA1("la1", "americas", "LAN"),
    LA2("la2", "americas", "LAS"),
    NA1("na1", "americas", "NA"),
    OC1("oc1", "sea", "OCE"),
    EUW1("euw1", "europe", "EUW"),
    EUN1("eun1", "europe", "EUNE"),
    TR1("tr1", "europe", "TR"),
    RU("ru", "europe", "RU"),
    KR("kr", "asia", "KR"),
    JP1("jp1", "asia", "JP");

    private final String platformHost;
    private final String regionalRouting;
    private final String shortLabel;

    RiotPlatformId(String platformHost, String regionalRouting, String shortLabel) {
        this.platformHost = platformHost;
        this.regionalRouting = regionalRouting;
        this.shortLabel = shortLabel;
    }

    public String platformHost() {
        return platformHost;
    }

    /** Ex.: americas — usado em {@code https://americas.api.riotgames.com}. */
    public String regionalRouting() {
        return regionalRouting;
    }

    public String shortLabel() {
        return shortLabel;
    }

    public static Optional<RiotPlatformId> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(p -> p.platformHost.equalsIgnoreCase(key) || p.name().equalsIgnoreCase(key))
                .findFirst();
    }
}
