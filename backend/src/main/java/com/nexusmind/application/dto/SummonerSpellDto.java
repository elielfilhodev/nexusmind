package com.nexusmind.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record SummonerSpellDto(String riotKey, String name, Integer cooldownSec, JsonNode modes) {
}
