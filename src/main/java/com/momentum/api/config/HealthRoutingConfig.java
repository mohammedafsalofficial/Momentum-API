package com.momentum.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class HealthRoutingConfig {

    @Bean
    public RouterFunction<ServerResponse> healthRoutes() {
        return RouterFunctions.route()
                .GET("/api/health", request ->
                        ServerResponse.ok().render("forward:/actuator/health"))
                .GET("/api/healthz", request ->
                        ServerResponse.ok().render("forward:/actuator/health/liveness"))
                .GET("/api/livez", request ->
                        ServerResponse.ok().render("forward:/actuator/health/liveness"))
                .GET("/api/readyz", request ->
                        ServerResponse.ok().render("forward:/actuator/health/readiness"))
                .build();
    }
}
