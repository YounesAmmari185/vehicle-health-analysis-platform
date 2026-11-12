package com.rdm.analysis.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.rdm.analysis.dto.RecallResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.junit.jupiter.api.Assertions.*;

import feign.FeignException;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/analysis_db",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.datasource.driver-class-name=org.postgresql.Driver"
})
class RecallFeignClientIntegrationTest {

    private static final WireMockServer wireMockServer =
            new WireMockServer(wireMockConfig().dynamicPort());

    static {
        wireMockServer.start();
    }

    @Autowired
    private RecallFeignClient recallClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("recall.service.url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }



    @Test
    void shouldReturnActiveRecall() {

        String vin = "VF1AAAAA123456789";

        wireMockServer.stubFor(get(urlEqualTo("/api/recalls/" + vin))
                .willReturn(okJson("""
                        {
                            "active": true,
                            "campaignName": "Campaign 2026",
                            "description": "Engine recall campaign"
                        }
                        """)));

        RecallResponse response = recallClient.getRecall(vin);

        assertNotNull(response);
        assertTrue(response.active());
        assertEquals("Campaign 2026", response.campaignName());
        assertEquals(
                "Engine recall campaign",
                response.description()
        );

        wireMockServer.verify(
                1,
                getRequestedFor(urlEqualTo("/api/recalls/" + vin))
        );
    }



    @Test
    void shouldReturnInactiveRecall() {

        String vin = "VF1AAAAA123456789";

        wireMockServer.stubFor(get(urlEqualTo("/api/recalls/" + vin))
                .willReturn(okJson("""
                        {
                            "active": false,
                            "campaignName": null,
                            "description": null
                        }
                        """)));

        RecallResponse response = recallClient.getRecall(vin);

        assertNotNull(response);
        assertFalse(response.active());
        assertNull(response.campaignName());
        assertNull(response.description());

        wireMockServer.verify(
                1,
                getRequestedFor(urlEqualTo("/api/recalls/" + vin))
        );
    }


    @Test
    void shouldThrowExceptionWhenRecallApiReturns500() {

        String vin = "VF1AAAAA123456789";

        wireMockServer.stubFor(get(urlEqualTo("/api/recalls/" + vin))
                .willReturn(serverError()));

        assertThrows(
                FeignException.class,
                () -> recallClient.getRecall(vin)
        );

        wireMockServer.verify(
                1,
                getRequestedFor(urlEqualTo("/api/recalls/" + vin))
        );
    }
}
