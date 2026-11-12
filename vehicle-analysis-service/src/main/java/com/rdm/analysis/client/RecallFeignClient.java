package com.rdm.analysis.client;

import com.rdm.analysis.dto.RecallResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "recall-service",
        url = "${recall.service.url}"
)
public interface RecallFeignClient {

    @GetMapping("/api/recalls/{vin}")
    RecallResponse getRecall(@PathVariable("vin") String vin);
}
