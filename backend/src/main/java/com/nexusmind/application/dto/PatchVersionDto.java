package com.nexusmind.application.dto;

import java.time.Instant;

public record PatchVersionDto(String version, boolean current, Instant releasedAt) {
}
