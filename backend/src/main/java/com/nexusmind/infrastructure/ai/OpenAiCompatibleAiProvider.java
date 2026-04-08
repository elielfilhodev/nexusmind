package com.nexusmind.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmind.application.ai.AiCompletionRequest;
import com.nexusmind.application.ai.AiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai_compatible", matchIfMissing = true)
public class OpenAiCompatibleAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiProvider.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiProperties props;

    public OpenAiCompatibleAiProvider(ObjectMapper objectMapper, AiProperties props) {
        this.objectMapper = objectMapper;
        this.props = props;
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofSeconds(Math.min(30, props.timeoutSeconds())).toMillis());
        rf.setReadTimeout((int) Duration.ofSeconds(props.timeoutSeconds()).toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(trimSlash(props.baseUrl()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(rf)
                .build();
    }

    @Override
    public String complete(AiCompletionRequest request) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            log.warn("AI_API_KEY ausente — pulando chamada ao provedor");
            return "";
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.model());
        body.put("temperature", 0.35);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        String user = request.userMessage();
        if (request.jsonOnly()) {
            user = user + "\n\nResponda somente JSON, sem markdown.";
        }
        messages.add(Map.of("role", "user", "content", user));
        body.put("messages", messages);

        int attempts = 0;
        int max = Math.max(1, props.maxRetries() + 1);
        while (attempts < max) {
            attempts++;
            try {
                String raw = restClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                        .body(body)
                        .retrieve()
                        .body(String.class);
                String text = extractAssistantText(raw);
                if (text.isBlank() && raw != null && !raw.isBlank()) {
                    log.warn(
                            "Resposta IA 200 sem texto extraível (verifique modelo/corpo). Trecho: {}",
                            raw.length() > 600 ? raw.substring(0, 600) + "…" : raw
                    );
                }
                return text;
            } catch (RestClientResponseException ex) {
                int status = ex.getStatusCode().value();
                String errBody = ex.getResponseBodyAsString();
                log.warn(
                        "Erro HTTP da API de IA {} — {}. Corpo: {}",
                        status,
                        ex.getStatusText(),
                        errBody != null && errBody.length() > 800 ? errBody.substring(0, 800) + "…" : errBody
                );
                if (attempts >= max) {
                    return "";
                }
                backoffIfRateLimitedOrOverloaded(status, attempts);
            } catch (RestClientException ex) {
                log.warn("Falha na chamada IA (tentativa {}/{}): {}", attempts, max, ex.getMessage());
                if (attempts >= max) {
                    return "";
                }
            }
        }
        return "";
    }

    private String extractAssistantText(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.has("error")) {
                JsonNode err = root.get("error");
                log.warn(
                        "API de IA retornou JSON de erro: {}",
                        err.isObject() ? err.path("message").asText(err.toString()) : err.toString()
                );
                return "";
            }
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode msg = choices.get(0).path("message");
                if (msg.hasNonNull("content")) {
                    return msg.path("content").asText("");
                }
                // Alguns fluxos retornam tool_calls em vez de content
                if (msg.has("tool_calls")) {
                    log.warn("Resposta IA só continha tool_calls — modelo não devolveu texto.");
                }
            }
        } catch (Exception e) {
            log.debug("Resposta IA não-JSON ou formato inesperado");
        }
        return "";
    }

    /** 429 / 502 / 503: rate limit ou modelo sobrecarregado (comum no Gemini) — espera antes de retentar. */
    private static void backoffIfRateLimitedOrOverloaded(int httpStatus, int attemptJustFinished) {
        if (httpStatus != 429 && httpStatus != 502 && httpStatus != 503) {
            return;
        }
        long ms = Math.min(12_000L, 600L * (1L << Math.min(attemptJustFinished - 1, 4)));
        log.info("Aguardando {} ms antes de nova tentativa à API de IA (HTTP {})", ms, httpStatus);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String trimSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
