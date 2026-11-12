package com.rdm.analysis.repository;

import com.rdm.analysis.entity.HealthReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HealthReportRepository
        extends JpaRepository<HealthReportEntity, UUID> {

    Optional<HealthReportEntity> findTopByVinOrderByAnalyzedAtDesc(String vin);
}
