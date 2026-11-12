
package com.rdm.analysis.service;

import com.rdm.analysis.client.RecallFeignClient;
import com.rdm.analysis.dto.AnalysisRequest;
import com.rdm.analysis.dto.RecallResponse;
import com.rdm.analysis.entity.AlertEntity;
import com.rdm.analysis.entity.HealthReportEntity;
import com.rdm.analysis.mapper.AnalysisMapper;
import com.rdm.analysis.repository.AlertRepository;
import com.rdm.analysis.repository.HealthReportRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleAnalysisServiceTest {

    @Mock
    private HealthReportRepository reportRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AnalysisMapper mapper;

    @Mock
    private RecallFeignClient recallClient;

    @InjectMocks
    private VehicleAnalysisService service;


    // TEST 1 : Véhicule en bon état (100 points)

    @Test
    void shouldReturnGoodStatusWhenVehicleHasNoIssues() {

        AnalysisRequest request = createRequest(
                30000L,
                "2.5", "2.5", "2.5", "2.5",
                "12.6", "90", "70",
                List.of()
        );

        mockRecall(false);
        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(100, report.getHealthScore());
        assertEquals("GOOD", report.getStatus());
        assertEquals(
                "Le véhicule est en bon état.",
                report.getSummary()
        );

        verify(alertRepository, never()).saveAll(anyList());
    }


    // TEST 2 : Quelques anomalies (60 points)

    @Test
    void shouldCalculateHealthScoreCorrectly() {

        AnalysisRequest request = createRequest(
                120000L,
                "1.8", "2.5", "2.5", "2.5",
                "11.8", "90", "50",
                List.of("P0420")
        );

        mockRecall(false);
        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(60, report.getHealthScore());
        assertEquals("WARNING", report.getStatus());
        assertEquals(
                "Le véhicule nécessite une attention particulière.",
                report.getSummary()
        );

        verify(alertRepository).saveAll(anyList());
    }


    // TEST 3 : Véhicule en état critique (0 point)

    @Test
    void shouldReturnCriticalStatusWhenVehicleHasManyIssues() {

        AnalysisRequest request = createRequest(
                200000L,
                "1.5", "1.5", "1.5", "1.5",
                "11.0", "120", "5",
                List.of("P0420", "P0300", "P0171", "P0100", "P0200")
        );

        mockRecall(true);
        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(0, report.getHealthScore());
        assertEquals("CRITICAL", report.getStatus());
        assertEquals(
                "Le véhicule nécessite une intervention.",
                report.getSummary()
        );

        verify(alertRepository).saveAll(anyList());
    }


    // TEST 4 : Rappel constructeur actif (-15 points)

    @Test
    void shouldApplyPenaltyWhenRecallIsActive() {

        AnalysisRequest request = createRequest(
                30000L,
                "2.5", "2.5", "2.5", "2.5",
                "12.6", "90", "70",
                List.of()
        );

        mockRecall(true);
        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(85, report.getHealthScore());
        assertEquals("GOOD", report.getStatus());

        verify(recallClient).getRecall(request.getVin());
        verify(alertRepository).saveAll(anyList());
    }


    // TEST 5 : API Recall indisponible

    @Test
    void shouldContinueAnalysisWhenRecallApiFails() {

        AnalysisRequest request = createRequest(
                30000L,
                "2.5", "2.5", "2.5", "2.5",
                "12.6", "90", "70",
                List.of()
        );

        when(recallClient.getRecall(request.getVin()))
                .thenThrow(new RuntimeException("API unavailable"));

        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(100, report.getHealthScore());
        assertEquals("GOOD", report.getStatus());

        verify(recallClient).getRecall(request.getVin());
        verify(alertRepository, never()).saveAll(anyList());
    }


    // TEST 6 : Pénalité des codes erreur plafonnée à 20 points

    @Test
    void shouldLimitErrorCodesPenaltyToTwentyPoints() {

        AnalysisRequest request = createRequest(
                30000L,
                "2.5", "2.5", "2.5", "2.5",
                "12.6", "90", "70",
                List.of(
                        "P0420",
                        "P0300",
                        "P0171",
                        "P0100",
                        "P0200",
                        "P0500"
                )
        );

        mockRecall(false);
        mockReportSaving();

        service.analyze(request);

        HealthReportEntity report = captureReport();

        assertEquals(80, report.getHealthScore());
        assertEquals("GOOD", report.getStatus());

        verify(alertRepository).saveAll(anyList());
    }


    // MÉTHODES UTILITAIRES

    private AnalysisRequest createRequest(
            long mileage,
            String frontLeft,
            String frontRight,
            String rearLeft,
            String rearRight,
            String battery,
            String temperature,
            String fuel,
            List<String> errorCodes) {

        return new AnalysisRequest(
                UUID.randomUUID(),
                "VF1AAAAA123456789",
                mileage,
                new BigDecimal(frontLeft),
                new BigDecimal(frontRight),
                new BigDecimal(rearLeft),
                new BigDecimal(rearRight),
                new BigDecimal(battery),
                new BigDecimal(temperature),
                new BigDecimal(fuel),
                errorCodes,
                Instant.now()
        );
    }

    private void mockRecall(boolean active) {

        when(recallClient.getRecall(any()))
                .thenReturn(new RecallResponse(
                        active,
                        active ? "Campaign 2026" : null,
                        active ? "Recall description" : null
                ));
    }

    private void mockReportSaving() {

        when(reportRepository.save(any(HealthReportEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private HealthReportEntity captureReport() {

        ArgumentCaptor<HealthReportEntity> captor =
                ArgumentCaptor.forClass(HealthReportEntity.class);

        verify(reportRepository).save(captor.capture());

        return captor.getValue();
    }
}
