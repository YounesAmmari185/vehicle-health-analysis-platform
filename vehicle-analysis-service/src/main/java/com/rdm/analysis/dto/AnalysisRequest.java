package com.rdm.analysis.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotNull
    private UUID telemetryId;

    @NotBlank
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$")
    private String vin;

    @NotNull
    @PositiveOrZero
    private Long mileage;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("6.0")
    private BigDecimal tirePressureFrontLeft;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("6.0")
    private BigDecimal tirePressureFrontRight;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("6.0")
    private BigDecimal tirePressureRearLeft;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("6.0")
    private BigDecimal tirePressureRearRight;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("20.0")
    private BigDecimal batteryVoltage;

    @NotNull
    @DecimalMin("-40.0")
    @DecimalMax("200.0")
    private BigDecimal engineTemperature;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal fuelLevel;

    private List<@NotBlank String> errorCodes;

    @NotNull
    private Instant recordedAt;
}
