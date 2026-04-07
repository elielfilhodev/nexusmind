package com.nexusmind.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportSummaryDto(String kind, UUID id, Instant createdAt, String summary) {
}
