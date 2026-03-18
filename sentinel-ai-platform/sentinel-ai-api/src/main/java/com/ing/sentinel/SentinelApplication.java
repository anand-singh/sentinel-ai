package com.ing.sentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sentinel AI REST API — Spring Boot entry point.
 *
 * Starts an embedded Tomcat on port 8080 and registers all
 * @RestController / @Service beans under com.ing.sentinel.
 * The ADK agents are invoked programmatically via OrchestratorService.
 */
@SpringBootApplication
public class SentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelApplication.class, args);
    }
}
