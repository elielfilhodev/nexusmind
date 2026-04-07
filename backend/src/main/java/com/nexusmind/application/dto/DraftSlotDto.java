package com.nexusmind.application.dto;

import jakarta.validation.constraints.Size;

public record DraftSlotDto(
        @Size(max = 64) String top,
        @Size(max = 64) String jungle,
        @Size(max = 64) String mid,
        @Size(max = 64) String adc,
        @Size(max = 64) String support
) {
}
