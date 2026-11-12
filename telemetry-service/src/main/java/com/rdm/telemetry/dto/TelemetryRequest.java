package com.rdm.telemetry.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TelemetryRequest(

        @NotBlank(message = "Le VIN est obligatoire")
        @Pattern(
                regexp = "^[A-HJ-NPR-Z0-9]{17}$",
                message = "Le VIN doit contenir exactement 17 caractères valides"
        )
        String vin,

        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage doit être positif ou nul")
        Long mileage,

        @NotNull(message = "La pression du pneu est obligatoire")
        @DecimalMin(value = "0.5", message = "Pression minimale : 0.5 bar")
        @DecimalMax(value = "6.0", message = "Pression maximale : 6.0 bars")
        BigDecimal tirePressureFrontLeft,

        @NotNull
        @DecimalMin("0.5")
        @DecimalMax("6.0")
        BigDecimal tirePressureFrontRight,

        @NotNull
        @DecimalMin("0.5")
        @DecimalMax("6.0")
        BigDecimal tirePressureRearLeft,

        @NotNull
        @DecimalMin("0.5")
        @DecimalMax("6.0")
        BigDecimal tirePressureRearRight,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("20.0")
        BigDecimal batteryVoltage,

        @NotNull
        @DecimalMin("-40.0")
        @DecimalMax("200.0")
        BigDecimal engineTemperature,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal fuelLevel,

        List<@NotBlank String> errorCodes,

        @NotNull(message = "La date de télémétrie est obligatoire")
        Instant recordedAt

) {}
