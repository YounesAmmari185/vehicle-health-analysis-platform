
package com.rdm.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.rdm.telemetry.client")
public class TelemetryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelemetryServiceApplication.class, args);
	}
}
