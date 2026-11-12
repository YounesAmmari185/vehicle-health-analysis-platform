package com.rdm.analysis.dto;

import java.time.Instant;
import java.util.UUID;

public record HealthReportResponse(
        UUID id,
        UUID telemetryId,
        String vin,
        int healthScore,
        String status,
        String summary,
        Instant analyzedAt
) {}
