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
                return extractAssistantText(raw);
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
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText("");
            }
        } catch (Exception e) {
            log.debug("Resposta IA não-JSON ou formato inesperado");
        }
        return "";
    }

    private static String trimSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
