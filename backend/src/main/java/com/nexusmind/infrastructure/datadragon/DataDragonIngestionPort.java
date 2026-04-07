package com.nexusmind.infrastructure.datadragon;

/**
 * Ponto de extensão para sincronizar campeões, itens, runas e spells a partir do Data Dragon
 * ou de um CDN espelhado. MVP: sem implementação ativa — dados vêm de {@code Flyway} seed.
 * <p>
 * Futuro: job agendado + versionamento por {@code patch_versions.version} alinhado ao
 * {@code https://ddragon.leagueoflegends.com/api/versions.json}.
 */
public interface DataDragonIngestionPort {

    /**
     * @param riotPatchVersion ex.: "15.5.1"
     */
    void syncPatch(String riotPatchVersion);
}
