package com.nexusmind.application.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StructuredJsonExtractor {

    private static final Logger log = LoggerFactory.getLogger(StructuredJsonExtractor.class);
    private static final Pattern FENCED = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;

    public StructuredJsonExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<JsonNode> extract(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        Optional<JsonNode> direct = tryParse(trimmed);
        if (direct.isPresent()) {
            return direct;
        }
        Matcher m = FENCED.matcher(raw);
        if (m.find()) {
            return tryParse(m.group(1).trim());
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return tryParse(trimmed.substring(start, end + 1));
        }
        return Optional.empty();
    }

    private Optional<JsonNode> tryParse(String s) {
        try {
            return Optional.of(objectMapper.readTree(s));
        } catch (JsonProcessingException e) {
            log.debug("JSON parse falhou: {}", e.getOriginalMessage());
            return Optional.empty();
        }
    }
}
