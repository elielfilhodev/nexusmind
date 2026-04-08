package com.nexusmind.infrastructure.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Client HTTP dedicado à Riot API — isolamento, retries controlados e logs sem segredos.
 */
@Component
public class RiotApiClient {

    private static final Logger log = LoggerFactory.getLogger(RiotApiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RiotProperties props;
    private final RiotRateLimitHandler rateLimitHandler;

    public RiotApiClient(ObjectMapper objectMapper, RiotProperties props, RiotRateLimitHandler rateLimitHandler) {
        this.objectMapper = objectMapper;
        this.props = props;
        this.rateLimitHandler = rateLimitHandler;
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) props.connectTimeout().toMillis());
        rf.setReadTimeout((int) props.readTimeout().toMillis());
        this.restClient = RestClient.builder().requestFactory(rf).build();
    }

    public boolean isConfigured() {
        return props.apiKey() != null && !props.apiKey().isBlank();
    }

    public Optional<JsonNode> getAccountByRiotId(RiotPlatformId platform, String gameName, String tagLine) {
        requireKey();
        String routing = platform.regionalRouting();
        String gn = UriUtils.encodePathSegment(gameName, StandardCharsets.UTF_8);
        String tg = UriUtils.encodePathSegment(tagLine, StandardCharsets.UTF_8);
        String url = "https://" + routing + ".api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + gn + "/" + tg;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getAccountByPuuid(RiotPlatformId platform, String puuid) {
        requireKey();
        String routing = platform.regionalRouting();
        String id = UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8);
        String url = "https://" + routing + ".api.riotgames.com/riot/account/v1/accounts/by-puuid/" + id;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getSummonerByPuuid(RiotPlatformId platform, String puuid) {
        requireKey();
        String host = platform.platformHost();
        String id = UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/summoner/v4/summoners/by-puuid/" + id;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getLeagueEntriesByPuuid(RiotPlatformId platform, String puuid) {
        requireKey();
        String host = platform.platformHost();
        String id = UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/league/v4/entries/by-puuid/" + id;
        return getJsonOptional(url, 404);
    }

    /** Retorna LeagueList (Challenger) ou vazio se 404. */
    public Optional<JsonNode> getChallengerLeague(RiotPlatformId platform, String queue) {
        requireKey();
        String host = platform.platformHost();
        String q = UriUtils.encodePathSegment(queue, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/" + q;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getGrandmasterLeague(RiotPlatformId platform, String queue) {
        requireKey();
        String host = platform.platformHost();
        String q = UriUtils.encodePathSegment(queue, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/league/v4/grandmasterleagues/by-queue/" + q;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getMatchIds(
            RiotPlatformId platform,
            String puuid,
            int start,
            int count,
            Optional<Integer> queueId
    ) {
        requireKey();
        String host = platform.platformHost();
        String id = UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("https://")
                .append(host)
                .append(".api.riotgames.com/lol/match/v5/matches/by-puuid/")
                .append(id)
                .append("/ids?start=")
                .append(Math.max(0, start))
                .append("&count=")
                .append(Math.min(100, Math.max(1, count)));
        queueId.ifPresent(q -> sb.append("&queue=").append(q));
        return getJsonOptional(sb.toString(), 404);
    }

    public Optional<JsonNode> getMatch(RiotPlatformId platform, String matchId) {
        requireKey();
        String host = platform.platformHost();
        String mid = UriUtils.encodePathSegment(matchId, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/match/v5/matches/" + mid;
        return getJsonOptional(url, 404);
    }

    public Optional<JsonNode> getChampionMasteriesTop(RiotPlatformId platform, String puuid, int count) {
        requireKey();
        String host = platform.platformHost();
        String id = UriUtils.encodePathSegment(puuid, StandardCharsets.UTF_8);
        int c = Math.min(16, Math.max(1, count));
        String url = "https://" + host + ".api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/" + id + "/top?count=" + c;
        return getJsonOptional(url, 404);
    }

    /** Summoner v4 por encrypted summoner id (usado quando League entry não expõe puuid). */
    public Optional<JsonNode> getSummonerByEncryptedId(RiotPlatformId platform, String encryptedSummonerId) {
        requireKey();
        String host = platform.platformHost();
        String id = UriUtils.encodePathSegment(encryptedSummonerId, StandardCharsets.UTF_8);
        String url = "https://" + host + ".api.riotgames.com/lol/summoner/v4/summoners/" + id;
        return getJsonOptional(url, 404);
    }

    private void requireKey() {
        if (!isConfigured()) {
            throw new RiotApiException(503, "RIOT_API_KEY não configurada no servidor");
        }
    }

    private Optional<JsonNode> getJsonOptional(String url, int treatAsEmptyStatus) {
        try {
            String body = getString(url);
            if (body == null || body.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readTree(body));
        } catch (RiotApiException ex) {
            if (ex.status() == treatAsEmptyStatus) {
                return Optional.empty();
            }
            throw ex;
        } catch (Exception e) {
            throw new RiotApiException(502, "Falha ao interpretar resposta da Riot");
        }
    }

    private String getString(String url) {
        int max = Math.max(1, props.maxRetries() + 1);
        int attempt = 0;
        while (attempt < max) {
            attempt++;
            rateLimitHandler.acquireOutboundSlot();
            try {
                return restClient.get()
                        .uri(URI.create(url))
                        .header("X-Riot-Token", props.apiKey())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException ex) {
                int st = ex.getStatusCode().value();
                if (st == 429) {
                    rateLimitHandler.backoffAfter429(ex);
                    if (attempt >= max) {
                        throw new RiotApiException(429, "Limite de requisições da Riot API atingido");
                    }
                    continue;
                }
                if (st == 404) {
                    throw new RiotApiException(404, "Recurso não encontrado na Riot API");
                }
                if (st == 403 || st == 401) {
                    log.warn("Riot API retornou {} — verifique RIOT_API_KEY e permissões", st);
                    throw new RiotApiException(st, "Integração Riot indisponível (credenciais)");
                }
                log.debug("Riot HTTP {} em {}", st, safeUrlForLog(url));
                throw new RiotApiException(st, "Erro ao consultar Riot API");
            } catch (RestClientException ex) {
                log.warn("Falha de rede Riot (tentativa {}/{}): {}", attempt, max, ex.getMessage());
                if (attempt >= max) {
                    throw new RiotApiException(502, "Falha temporária ao contatar a Riot API");
                }
                sleepBackoff(attempt);
            }
        }
        throw new RiotApiException(502, "Falha temporária ao contatar a Riot API");
    }

    private static void sleepBackoff(int attempt) {
        long ms = Math.min(10_000L, 400L * (1L << Math.min(attempt - 1, 4)));
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String safeUrlForLog(String url) {
        try {
            URI u = URI.create(url);
            return u.getPath();
        } catch (Exception e) {
            return "(url)";
        }
    }
}
