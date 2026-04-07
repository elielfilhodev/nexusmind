package com.nexusmind.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record GameItemDto(String riotKey, String name, String description, Integer goldCost, JsonNode stats) {
}
