package com.rdm.telemetry.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisRequest(
        UUID telemetryId,
        String vin,
        Long mileage,
        BigDecimal tirePressureFrontLeft,
        BigDecimal tirePressureFrontRight,
        BigDecimal tirePressureRearLeft,
        BigDecimal tirePressureRearRight,
        BigDecimal batteryVoltage,
        BigDecimal engineTemperature,
        BigDecimal fuelLevel,
        List<String> errorCodes,
        Instant recordedAt
) {}
