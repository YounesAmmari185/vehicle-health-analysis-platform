package com.rdm.analysis.repository;

import com.rdm.analysis.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findByVinOrderByCreatedAtDesc(String vin);
}