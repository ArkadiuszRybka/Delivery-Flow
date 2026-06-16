package com.deliveryflow.gateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("/api/v1/orders/**")
                        .or(RequestPredicates.path("/api/v1/products/**"))
                        .or(RequestPredicates.path("/api/v1/admin/**"))
                        .or(RequestPredicates.path("/api/v1/auth/**"))
                        .or(RequestPredicates.path("/api/v1/webhooks/**")),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> trackingServiceRoute() {
        return GatewayRouterFunctions.route("tracking-service")
                .route(RequestPredicates.path("/api/v1/tracking/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .build();
    }
}
