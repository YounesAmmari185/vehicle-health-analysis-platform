package com.rdm.analysis.service;

import com.rdm.analysis.client.RecallFeignClient;
import com.rdm.analysis.dto.*;
import com.rdm.analysis.entity.*;
import com.rdm.analysis.mapper.AnalysisMapper;
import com.rdm.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleAnalysisService {

    private final HealthReportRepository reportRepository;
    private final AlertRepository alertRepository;
    private final AnalysisMapper mapper;
    private final RecallFeignClient recallClient;

    @Transactional
    public HealthReportResponse analyze(AnalysisRequest request) {

        List<AlertEntity> alerts = new ArrayList<>();

        int score = 100;

        score -= analyzeMileage(request, alerts);
        score -= analyzeTires(request, alerts);
        score -= analyzeBattery(request, alerts);
        score -= analyzeEngineTemperature(request, alerts);
        score -= analyzeFuel(request, alerts);
        score -= analyzeErrorCodes(request, alerts);
        score -= analyzeRecall(request, alerts);

        score = Math.max(0, Math.min(score, 100));

        String status = getStatus(score);

        HealthReportEntity report = HealthReportEntity.builder()
                .id(UUID.randomUUID())
                .telemetryId(request.getTelemetryId())
                .vin(request.getVin())
                .healthScore(score)
                .status(status)
                .summary(getSummary(status))
                .analyzedAt(Instant.now())
                .build();

        HealthReportEntity savedReport = reportRepository.save(report);

        if (!alerts.isEmpty()) {
            alertRepository.saveAll(alerts);
        }

        log.info("Analysis completed for VIN {} with score {}",
                request.getVin(), score);

        return mapper.toHealthReportResponse(savedReport);
    }

    private int analyzeMileage(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        long mileage = request.getMileage();

        if (mileage > 150000) {
            alerts.add(createAlert(request, "MILEAGE", "HIGH",
                    "Le kilométrage dépasse 150 000 km."));
            return 15;
        }

        if (mileage > 100000) {
            alerts.add(createAlert(request, "MILEAGE", "MEDIUM",
                    "Le véhicule a dépassé 100 000 km."));
            return 10;
        }

        if (mileage > 50000) {
            alerts.add(createAlert(request, "MILEAGE", "LOW",
                    "Le véhicule a dépassé 50 000 km."));
            return 5;
        }

        return 0;
    }

    private int analyzeTires(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        int penalty = 0;

        penalty += checkTire(request.getTirePressureFrontLeft(),
                "Pneu avant gauche", request, alerts);

        penalty += checkTire(request.getTirePressureFrontRight(),
                "Pneu avant droit", request, alerts);

        penalty += checkTire(request.getTirePressureRearLeft(),
                "Pneu arrière gauche", request, alerts);

        penalty += checkTire(request.getTirePressureRearRight(),
                "Pneu arrière droit", request, alerts);

        return penalty;
    }

    private int checkTire(
            BigDecimal pressure,
            String tireName,
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        if (pressure.compareTo(new BigDecimal("2.0")) < 0) {
            alerts.add(createAlert(request, "TIRE_PRESSURE", "HIGH",
                    tireName + " : pression insuffisante."));
            return 10;
        }

        if (pressure.compareTo(new BigDecimal("2.8")) > 0) {
            alerts.add(createAlert(request, "TIRE_PRESSURE", "HIGH",
                    tireName + " : pression trop élevée."));
            return 10;
        }

        return 0;
    }

    private int analyzeBattery(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        if (request.getBatteryVoltage().compareTo(
                new BigDecimal("12.0")) < 0) {

            alerts.add(createAlert(request, "BATTERY", "HIGH",
                    "La tension de la batterie est faible."));

            return 15;
        }

        return 0;
    }

    private int analyzeEngineTemperature(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        if (request.getEngineTemperature().compareTo(
                new BigDecimal("110")) > 0) {

            alerts.add(createAlert(request, "ENGINE_TEMPERATURE",
                    "CRITICAL",
                    "La température du moteur est trop élevée."));

            return 20;
        }

        return 0;
    }

    private int analyzeFuel(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        if (request.getFuelLevel().compareTo(
                new BigDecimal("10")) < 0) {

            alerts.add(createAlert(request, "FUEL", "LOW",
                    "Le niveau de carburant est faible."));

            return 5;
        }

        return 0;
    }

    private int analyzeErrorCodes(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        List<String> errorCodes = request.getErrorCodes() == null
                ? List.of()
                : request.getErrorCodes();

        for (String errorCode : errorCodes) {
            alerts.add(createAlert(request, "ERROR_CODE", "HIGH",
                    "Code erreur détecté : " + errorCode));
        }

        return Math.min(errorCodes.size() * 5, 20);
    }

    private int analyzeRecall(
            AnalysisRequest request,
            List<AlertEntity> alerts) {

        try {
            RecallResponse recall =
                    recallClient.getRecall(request.getVin());

            if (recall != null && recall.active()) {

                String message = "Campagne de rappel active : "
                        + recall.campaignName();

                alerts.add(createAlert(request, "RECALL", "HIGH", message));

                return 15;
            }

        } catch (Exception e) {
            log.warn("Recall API unavailable for VIN {}: {}",
                    request.getVin(), e.getMessage());
        }

        return 0;
    }

    private AlertEntity createAlert(
            AnalysisRequest request,
            String type,
            String severity,
            String message) {

        return AlertEntity.builder()
                .id(UUID.randomUUID())
                .telemetryId(request.getTelemetryId())
                .vin(request.getVin())
                .type(type)
                .severity(severity)
                .message(message)
                .createdAt(Instant.now())
                .build();
    }

    private String getStatus(int score) {
        if (score >= 80) {
            return "GOOD";
        }

        if (score >= 60) {
            return "WARNING";
        }

        return "CRITICAL";
    }

    private String getSummary(String status) {
        return switch (status) {
            case "GOOD" -> "Le véhicule est en bon état.";
            case "WARNING" ->
                    "Le véhicule nécessite une attention particulière.";
            default ->
                    "Le véhicule nécessite une intervention.";
        };
    }

    @Transactional(readOnly = true)
    public HealthReportResponse getHealthReport(String vin) {

        HealthReportEntity report = reportRepository
                .findTopByVinOrderByAnalyzedAtDesc(vin)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Aucun rapport trouvé pour ce véhicule : " + vin));

        return mapper.toHealthReportResponse(report);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(String vin) {

        return alertRepository.findByVinOrderByCreatedAtDesc(vin)
                .stream()
                .map(mapper::toAlertResponse)
                .toList();
    }
}
