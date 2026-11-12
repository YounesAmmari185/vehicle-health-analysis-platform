package com.rdm.telemetry.controller;

import com.rdm.telemetry.dto.TelemetryRequest;
import com.rdm.telemetry.dto.TelemetryResponse;
import com.rdm.telemetry.service.TelemetryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    public ResponseEntity<TelemetryResponse> register(
            @Valid @RequestBody TelemetryRequest request) {

        TelemetryResponse response = telemetryService.register(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}
