
package com.rdm.telemetry.dto;

import java.time.Instant;
import java.util.UUID;

public record TelemetryResponse(
        UUID id,
        String vin,
        String message,
        Instant recordedAt
) {}
