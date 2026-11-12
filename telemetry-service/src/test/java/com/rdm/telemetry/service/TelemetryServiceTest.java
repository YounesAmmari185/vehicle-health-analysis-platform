
package com.rdm.telemetry.service;

import com.rdm.telemetry.client.AnalysisFeignClient;
import com.rdm.telemetry.dto.AnalysisRequest;
import com.rdm.telemetry.dto.TelemetryRequest;
import com.rdm.telemetry.dto.TelemetryResponse;
import com.rdm.telemetry.entity.TelemetryEntity;
import com.rdm.telemetry.mapper.TelemetryMapper;
import com.rdm.telemetry.repository.TelemetryRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private TelemetryRepository repository;

    @Mock
    private TelemetryMapper mapper;

    @Mock
    private AnalysisFeignClient analysisClient;

    @InjectMocks
    private TelemetryService service;


    // TEST 1 : Sauvegarde correcte de la télémétrie

    @Test
    void shouldSaveTelemetrySuccessfully() {

        TelemetryRequest request = createRequest();

        TelemetryEntity entity = createEntity();

        TelemetryResponse expectedResponse =
                mock(TelemetryResponse.class);

        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository.save(any(TelemetryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(TelemetryEntity.class)))
                .thenReturn(expectedResponse);

        doNothing().when(analysisClient).analyze(any(AnalysisRequest.class));

        TelemetryResponse response = service.register(request);

        ArgumentCaptor<TelemetryEntity> captor =
                ArgumentCaptor.forClass(TelemetryEntity.class);

        verify(repository).save(captor.capture());

        TelemetryEntity saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals(entity.getVin(), saved.getVin());
        assertEquals(entity.getMileage(), saved.getMileage());

        assertSame(expectedResponse, response);

        verify(mapper).toEntity(request);
        verify(mapper).toResponse(saved);
        verify(analysisClient).analyze(any(AnalysisRequest.class));
    }


    // TEST 2 : Génération d'un identifiant UUID

    @Test
    void shouldGenerateTelemetryIdBeforeSaving() {

        TelemetryRequest request = createRequest();

        TelemetryEntity entity = createEntity();

        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository.save(any(TelemetryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(TelemetryEntity.class)))
                .thenReturn(mock(TelemetryResponse.class));

        doNothing().when(analysisClient).analyze(any(AnalysisRequest.class));

        service.register(request);

        assertNotNull(entity.getId());

        verify(repository).save(entity);
    }


    // TEST 3 : Déclenchement du microservice d'analyse

    @Test
    void shouldTriggerAnalysisAfterSavingTelemetry() {

        TelemetryRequest request = createRequest();

        TelemetryEntity entity = createEntity();

        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository.save(any(TelemetryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(TelemetryEntity.class)))
                .thenReturn(mock(TelemetryResponse.class));

        doNothing().when(analysisClient).analyze(any(AnalysisRequest.class));

        service.register(request);

        ArgumentCaptor<AnalysisRequest> captor =
                ArgumentCaptor.forClass(AnalysisRequest.class);

        verify(analysisClient).analyze(captor.capture());

        AnalysisRequest analysisRequest = captor.getValue();

        assertEquals(entity.getId(), analysisRequest.telemetryId());
        assertEquals(entity.getVin(), analysisRequest.vin());
        assertEquals(entity.getMileage(), analysisRequest.mileage());
    }


    // TEST 4 : Continuer si le microservice d'analyse est indisponible

    @Test
    void shouldSaveTelemetryEvenWhenAnalysisServiceFails() {

        TelemetryRequest request = createRequest();

        TelemetryEntity entity = createEntity();

        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository.save(any(TelemetryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TelemetryResponse expectedResponse =
                mock(TelemetryResponse.class);

        when(mapper.toResponse(any(TelemetryEntity.class)))
                .thenReturn(expectedResponse);

        doThrow(new RuntimeException("Analysis service unavailable"))
                .when(analysisClient).analyze(any(AnalysisRequest.class));

        TelemetryResponse response = service.register(request);

        assertNotNull(entity.getId());
        assertSame(expectedResponse, response);

        verify(repository).save(entity);
        verify(analysisClient).analyze(any(AnalysisRequest.class));
        verify(mapper).toResponse(entity);
    }


    // TEST 5 : Vérifier les données envoyées au microservice d'analyse

    @Test
    void shouldSendCorrectTelemetryDataToAnalysisService() {

        TelemetryRequest request = createRequest();

        TelemetryEntity entity = createEntity();

        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository.save(any(TelemetryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(TelemetryEntity.class)))
                .thenReturn(mock(TelemetryResponse.class));

        doNothing().when(analysisClient).analyze(any(AnalysisRequest.class));

        service.register(request);

        ArgumentCaptor<AnalysisRequest> captor =
                ArgumentCaptor.forClass(AnalysisRequest.class);

        verify(analysisClient).analyze(captor.capture());

        AnalysisRequest analysisRequest = captor.getValue();

        assertEquals(entity.getId(), analysisRequest.telemetryId());
        assertEquals(entity.getVin(), analysisRequest.vin());
        assertEquals(entity.getMileage(), analysisRequest.mileage());

        assertEquals(
                entity.getTirePressureFrontLeft(),
                analysisRequest.tirePressureFrontLeft()
        );

        assertEquals(
                entity.getBatteryVoltage(),
                analysisRequest.batteryVoltage()
        );

        assertEquals(
                entity.getEngineTemperature(),
                analysisRequest.engineTemperature()
        );

        assertEquals(
                entity.getFuelLevel(),
                analysisRequest.fuelLevel()
        );

        assertEquals(
                entity.getErrorCodes(),
                analysisRequest.errorCodes()
        );

        assertEquals(
                entity.getRecordedAt(),
                analysisRequest.recordedAt()
        );
    }


    // MÉTHODES UTILITAIRES

    private TelemetryRequest createRequest() {

        return new TelemetryRequest(
                "VF1AAAAA123456789",
                120000L,
                new BigDecimal("1.8"),
                new BigDecimal("2.5"),
                new BigDecimal("2.5"),
                new BigDecimal("2.5"),
                new BigDecimal("11.8"),
                new BigDecimal("90"),
                new BigDecimal("50"),
                List.of("P0420"),
                Instant.now()
        );
    }

    private TelemetryEntity createEntity() {

        TelemetryEntity entity = new TelemetryEntity();

        entity.setVin("VF1AAAAA123456789");
        entity.setMileage(120000L);

        entity.setTirePressureFrontLeft(new BigDecimal("1.8"));
        entity.setTirePressureFrontRight(new BigDecimal("2.5"));
        entity.setTirePressureRearLeft(new BigDecimal("2.5"));
        entity.setTirePressureRearRight(new BigDecimal("2.5"));

        entity.setBatteryVoltage(new BigDecimal("11.8"));
        entity.setEngineTemperature(new BigDecimal("90"));
        entity.setFuelLevel(new BigDecimal("50"));

        entity.setErrorCodes(List.of("P0420"));
        entity.setRecordedAt(Instant.now());

        return entity;
    }
}
