package com.nexusmind.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CasualAnalysisRequestDto(
        @NotBlank @Size(max = 32) String elo,
        @NotBlank @Pattern(regexp = "TOP|JUNGLE|MID|ADC|SUPPORT|UTILITY") String lane,
        @NotBlank @Size(max = 32) String playstyle,
        @Size(max = 32) String region,
        @Size(max = 64) String favoriteChampionKey
) {
}
