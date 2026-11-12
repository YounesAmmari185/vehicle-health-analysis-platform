package com.rdm.telemetry.client;

import com.rdm.telemetry.dto.AnalysisRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "vehicle-analysis-service",
        url = "${analysis.service.url}"
)
public interface AnalysisFeignClient {

    @PostMapping("/internal/analysis")
    void analyze(@RequestBody AnalysisRequest request);
}
