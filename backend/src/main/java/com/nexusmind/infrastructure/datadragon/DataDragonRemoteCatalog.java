package com.nexusmind.infrastructure.datadragon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Lê o catálogo oficial de campeões do Data Dragon para enriquecer prompts de IA,
 * sem depender do seed limitado no banco.
 */
@Service
public class DataDragonRemoteCatalog {

    private static final Logger log = LoggerFactory.getLogger(DataDragonRemoteCatalog.class);

    private final ObjectMapper objectMapper;
    private final Cache<String, String> championSnippetByPatch = Caffeine.newBuilder()
            .maximumSize(32)
            .expireAfterWrite(Duration.ofHours(8))
            .build();

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://ddragon.leagueoflegends.com")
            .build();

    public DataDragonRemoteCatalog(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Lista "id:nome" de todos os campeões do patch, para contexto em prompts.
     */
    public Optional<String> getChampionSnippet(String patchVersion) {
        if (patchVersion == null || patchVersion.isBlank()) {
            return Optional.empty();
        }
        String cached = championSnippetByPatch.getIfPresent(patchVersion);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            Optional<String> parsed = fetchAndFormatChampionSnippet(patchVersion);
            if (parsed.isPresent()) {
                championSnippetByPatch.put(patchVersion, parsed.get());
                return parsed;
            }
            String latest = fetchLatestDdragonVersion();
            if (latest != null && !latest.equals(patchVersion)) {
                log.info("Patch {} não encontrado no CDN; tentando versão mais recente {}", patchVersion, latest);
                parsed = fetchAndFormatChampionSnippet(latest);
                parsed.ifPresent(s -> championSnippetByPatch.put(patchVersion, s));
                return parsed;
            }
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Falha ao obter catálogo de campeões Data Dragon: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> fetchAndFormatChampionSnippet(String cdnVersion) {
        try {
            String body = restClient.get()
                    .uri("/cdn/{v}/data/en_US/champion.json", cdnVersion)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                return Optional.empty();
            }
            JsonNode data = objectMapper.readTree(body).path("data");
            if (!data.isObject()) {
                return Optional.empty();
            }
            List<String> parts = new ArrayList<>();
            data.fields().forEachRemaining(e -> {
                JsonNode c = e.getValue();
                String id = c.path("id").asText("");
                String name = c.path("name").asText("");
                if (!id.isEmpty()) {
                    parts.add(id + ":" + name);
                }
            });
            Collections.sort(parts);
            return Optional.of(String.join(", ", parts));
        } catch (RestClientException ex) {
            log.debug("champion.json não disponível para {}: {}", cdnVersion, ex.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            log.debug("Parse champion.json falhou para {}: {}", cdnVersion, ex.getMessage());
            return Optional.empty();
        }
    }

    private String fetchLatestDdragonVersion() {
        try {
            String raw = restClient.get()
                    .uri("/api/versions.json")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            JsonNode arr = objectMapper.readTree(raw);
            if (!arr.isArray() || arr.isEmpty()) {
                return null;
            }
            return arr.get(0).asText(null);
        } catch (Exception ex) {
            log.debug("versions.json indisponível: {}", ex.getMessage());
            return null;
        }
    }
}
