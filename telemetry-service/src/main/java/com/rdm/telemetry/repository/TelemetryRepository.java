package com.rdm.telemetry.repository;

import com.rdm.telemetry.entity.TelemetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TelemetryRepository
        extends JpaRepository<TelemetryEntity, UUID> {
}
