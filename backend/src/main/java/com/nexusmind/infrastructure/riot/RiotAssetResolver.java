package com.nexusmind.infrastructure.riot;

import org.springframework.stereotype.Component;

/**
 * Resolve URLs públicas de assets (Data Dragon / CDN Riot) sem acoplar o domínio ao formato bruto da API.
 */
@Component
public class RiotAssetResolver {

    private static final String DDRAGON = "https://ddragon.leagueoflegends.com/cdn";

    public String profileIconUrl(String ddragonVersion, int profileIconId) {
        String v = ddragonVersion != null && !ddragonVersion.isBlank() ? ddragonVersion : "14.1.1";
        return DDRAGON + "/" + v + "/img/profileicon/" + profileIconId + ".png";
    }

    public String championSquareUrl(String ddragonVersion, String championImageFile) {
        String v = safeVersion(ddragonVersion);
        return DDRAGON + "/" + v + "/img/champion/" + championImageFile;
    }

    public String championSplashUrl(String ddragonVersion, String championIdForFile) {
        String v = safeVersion(ddragonVersion);
        String skinFile = championIdForFile + "_0.jpg";
        return "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + skinFile;
    }

    public String itemIconUrl(String ddragonVersion, int itemId) {
        String v = safeVersion(ddragonVersion);
        return DDRAGON + "/" + v + "/img/item/" + itemId + ".png";
    }

    /** Runas: paths relativos retornados pela API de match (perks) mapeados para o CDN img. */
    public String perkIconUrl(String iconPathFromApi) {
        if (iconPathFromApi == null || iconPathFromApi.isBlank()) {
            return "";
        }
        if (iconPathFromApi.startsWith("http")) {
            return iconPathFromApi;
        }
        String p = iconPathFromApi.startsWith("/") ? iconPathFromApi : "/" + iconPathFromApi;
        return "https://ddragon.leagueoflegends.com/cdn/img" + p;
    }

    public String rankedEmblemUrl(String tier) {
        if (tier == null || tier.isBlank()) {
            return "";
        }
        String t = tier.trim().toUpperCase();
        return "https://ddragon.leagueoflegends.com/cdn/5.5.1/img/tier/" + t + ".png";
    }

    private static String safeVersion(String ddragonVersion) {
        return ddragonVersion != null && !ddragonVersion.isBlank() ? ddragonVersion : "14.1.1";
    }
}
