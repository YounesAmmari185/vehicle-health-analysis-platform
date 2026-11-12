
package com.rdm.analysis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID telemetryId;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false)
    private int healthScore;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String summary;

    @Column(nullable = false)
    private Instant analyzedAt;
}
