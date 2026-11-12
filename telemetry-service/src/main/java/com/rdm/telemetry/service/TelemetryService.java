
package com.rdm.telemetry.service;

import com.rdm.telemetry.client.AnalysisFeignClient;
import com.rdm.telemetry.dto.AnalysisRequest;
import com.rdm.telemetry.dto.TelemetryRequest;
import com.rdm.telemetry.dto.TelemetryResponse;
import com.rdm.telemetry.entity.TelemetryEntity;
import com.rdm.telemetry.mapper.TelemetryMapper;
import com.rdm.telemetry.repository.TelemetryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final TelemetryRepository repository;
    private final TelemetryMapper mapper;
    private final AnalysisFeignClient analysisClient;

    public TelemetryResponse register(TelemetryRequest request) {

        // Generate ID
        TelemetryEntity entity = mapper.toEntity(request);
        entity.setId(UUID.randomUUID());

        // Save telemetry
        TelemetryEntity saved = repository.save(entity);

        // Trigger analysis
        try {
            log.info("Calling analysis service for telemetry {}", saved.getId());

            AnalysisRequest analysisRequest = new AnalysisRequest(
                    saved.getId(),
                    saved.getVin(),
                    saved.getMileage(),
                    saved.getTirePressureFrontLeft(),
                    saved.getTirePressureFrontRight(),
                    saved.getTirePressureRearLeft(),
                    saved.getTirePressureRearRight(),
                    saved.getBatteryVoltage(),
                    saved.getEngineTemperature(),
                    saved.getFuelLevel(),
                    saved.getErrorCodes(),
                    saved.getRecordedAt()
            );

            analysisClient.analyze(analysisRequest);

            log.info("Analysis triggered successfully for telemetry {}", saved.getId());

        } catch (Exception e) {
            log.error(
                    "Telemetry {} saved, but analysis failed",
                    saved.getId(),
                    e
            );
        }

        return mapper.toResponse(saved);
    }
}
