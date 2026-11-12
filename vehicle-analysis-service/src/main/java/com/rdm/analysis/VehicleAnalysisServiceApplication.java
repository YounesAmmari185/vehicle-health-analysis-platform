package com.rdm.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.rdm.analysis.client")
public class VehicleAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleAnalysisServiceApplication.class, args);
	}
}