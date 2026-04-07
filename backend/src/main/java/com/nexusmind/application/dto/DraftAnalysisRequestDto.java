package com.nexusmind.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DraftAnalysisRequestDto(
        @Pattern(regexp = "(BLUE|RED|UNKNOWN)?") String side,
        @Valid @NotNull DraftSlotDto ally,
        @Valid @NotNull DraftSlotDto enemy,
        @Size(max = 32) String contextType,
        @Size(max = 32) String strategicFocus
) {
}
