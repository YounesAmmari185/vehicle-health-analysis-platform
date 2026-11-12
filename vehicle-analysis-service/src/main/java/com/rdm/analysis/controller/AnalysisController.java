package com.rdm.analysis.controller;

import com.rdm.analysis.dto.*;
import com.rdm.analysis.service.VehicleAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalysisController {

    private final VehicleAnalysisService analysisService;

    @PostMapping("/internal/analysis")
    public ResponseEntity<HealthReportResponse> analyze(
            @Valid @RequestBody AnalysisRequest request) {

        HealthReportResponse response = analysisService.analyze(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/api/v1/vehicles/{vin}/health-report")
    public ResponseEntity<HealthReportResponse> getHealthReport(
            @PathVariable String vin) {

        return ResponseEntity.ok(
                analysisService.getHealthReport(vin));
    }

    @GetMapping("/api/v1/vehicles/{vin}/alerts")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @PathVariable String vin) {

        return ResponseEntity.ok(
                analysisService.getAlerts(vin));
    }
}
