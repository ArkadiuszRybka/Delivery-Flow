package com.deliveryflow.order.client;

import com.deliveryflow.order.dto.TrackingInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class TrackingClient {

    private final RestClient restClient;
    private final CacheManager cacheManager;

    public TrackingClient(TrackingClientProperties properties, CacheManager cacheManager, RestClient.Builder restClientBuilder) {
        var requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build(HttpClientSettings.defaults()
                        .withConnectTimeout(properties.connectTimeout())
                        .withReadTimeout(properties.readTimeout()));
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.cacheManager = cacheManager;
    }

    @CircuitBreaker(name = "trackingService", fallbackMethod = "createTrackingEntryFallback")
    @Retry(name = "trackingService")
    public void createTrackingEntry(UUID orderId, String status) {
        restClient.post()
                .uri("/api/v1/tracking")
                .body(new TrackingEntryRequest(orderId, status, null, null))
                .retrieve()
                .toBodilessEntity();
    }

    @Cacheable(value = "trackingStatus", key = "#orderId")
    @CircuitBreaker(name = "trackingService", fallbackMethod = "getLatestTrackingFallback")
    @Retry(name = "trackingService")
    public Optional<TrackingInfo> getLatestTracking(UUID orderId) {
        TrackingInfo info = restClient.get()
                .uri("/api/v1/tracking/{orderId}/latest", orderId)
                .retrieve()
                .body(TrackingInfo.class);
        return Optional.ofNullable(info);
    }

    private void createTrackingEntryFallback(UUID orderId, String status, Exception ex) {
        log.warn("Failed to create tracking entry for orderId={}, status={}: {}", orderId, status, ex.getMessage());
    }

    private Optional<TrackingInfo> getLatestTrackingFallback(UUID orderId, Exception ex) {
        log.warn("Failed to fetch latest tracking for orderId={}, serving last known state from cache: {}", orderId, ex.getMessage());
        Cache cache = cacheManager.getCache("trackingStatus");
        TrackingInfo cached = cache != null ? cache.get(orderId, TrackingInfo.class) : null;
        return Optional.ofNullable(cached);
    }

    private record TrackingEntryRequest(UUID orderId, String status, String location, String note) {
    }
}
