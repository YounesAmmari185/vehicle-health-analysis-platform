
package com.rdm.analysis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID telemetryId;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;
}
