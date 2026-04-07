package com.nexusmind.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ChampionDto(String riotKey, String name, String title, JsonNode lanes, JsonNode tags) {
}
