package com.nexusmind.application.competitive;

import java.time.Instant;

/**
 * Aproximação de temporadas — a Riot não expõe seasonId diretamente no Match-V5;
 * usamos cortes de data para agregar estatísticas.
 */
public final class SeasonCatalog {

    private SeasonCatalog() {
    }

    /** Identificador estável para UI e filtros. */
    public static String seasonLabelForGameStart(Instant gameStart) {
        long ms = gameStart.toEpochMilli();
        if (ms >= Instant.parse("2025-01-09T00:00:00Z").toEpochMilli()) {
            return "S15";
        }
        if (ms >= Instant.parse("2024-01-10T00:00:00Z").toEpochMilli()) {
            return "S14";
        }
        if (ms >= Instant.parse("2023-01-11T00:00:00Z").toEpochMilli()) {
            return "S13";
        }
        return "LEGACY";
    }
}
