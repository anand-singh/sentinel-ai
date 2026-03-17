package com.ing.sentinel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration — allows the Next.js dashboard to call the API.
 * In production the WEB_ORIGIN env-var should be set to the Cloud Run web URL.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        String webOrigin = System.getenv().getOrDefault("WEB_ORIGIN", "http://localhost:3000");

        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(webOrigin, "http://localhost:3000", "http://localhost:3001"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
