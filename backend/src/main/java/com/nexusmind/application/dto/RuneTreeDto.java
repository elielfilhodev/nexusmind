package com.nexusmind.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RuneTreeDto(String treeKey, String name, JsonNode slots) {
}
