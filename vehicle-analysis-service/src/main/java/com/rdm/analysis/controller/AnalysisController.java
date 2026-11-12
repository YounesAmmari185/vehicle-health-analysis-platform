package com.rdm.analysis.controller;

import com.rdm.analysis.dto.*;
import com.rdm.analysis.service.VehicleAnalysisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class AnalysisController {

    private final VehicleAnalysisService analysisService;

    private static final String VIN_PATTERN = "^[A-HJ-NPR-Z0-9]{17}$";
    private static final String VIN_MESSAGE =
            "Le VIN doit contenir exactement 17 caractères valides";

    @PostMapping("/internal/analysis")
    public ResponseEntity<HealthReportResponse> analyze(
            @Valid @RequestBody AnalysisRequest request) {

        HealthReportResponse response = analysisService.analyze(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/api/v1/vehicles/{vin}/health-report")
    public ResponseEntity<HealthReportResponse> getHealthReport(
            @PathVariable
            @Pattern(regexp = VIN_PATTERN, message = VIN_MESSAGE)
            String vin) {

        return ResponseEntity.ok(
                analysisService.getHealthReport(vin));
    }

    @GetMapping("/api/v1/vehicles/{vin}/alerts")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @PathVariable
            @Pattern(regexp = VIN_PATTERN, message = VIN_MESSAGE)
            String vin) {

        return ResponseEntity.ok(
                analysisService.getAlerts(vin));
    }
}
