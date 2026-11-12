package com.rdm.analysis.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String vin,
        String type,
        String severity,
        String message,
        Instant createdAt
) {}
