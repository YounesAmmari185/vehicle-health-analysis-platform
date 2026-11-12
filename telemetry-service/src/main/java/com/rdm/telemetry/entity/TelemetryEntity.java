package com.rdm.telemetry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "telemetry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false)
    private Long mileage;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tirePressureFrontLeft;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tirePressureFrontRight;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tirePressureRearLeft;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tirePressureRearRight;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal batteryVoltage;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal engineTemperature;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal fuelLevel;

    @ElementCollection
    @CollectionTable(
            name = "telemetry_error_codes",
            joinColumns = @JoinColumn(name = "telemetry_id")
    )
    @Column(name = "error_code")
    private List<String> errorCodes;

    @Column(nullable = false)
    private Instant recordedAt;
}
