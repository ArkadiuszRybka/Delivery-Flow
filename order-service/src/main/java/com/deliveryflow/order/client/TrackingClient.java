package com.deliveryflow.order.client;

import com.deliveryflow.order.dto.TrackingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class TrackingClient {

    private final RestClient restClient;

    public TrackingClient(TrackingClientProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    public void createTrackingEntry(UUID orderId, String status) {
        try {
            restClient.post()
                    .uri("/api/v1/tracking")
                    .body(new TrackingEntryRequest(orderId, status, null, null))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Failed to create tracking entry for orderId={}, status={}: {}", orderId, status, ex.getMessage());
        }
    }

    public Optional<TrackingInfo> getLatestTracking(UUID orderId) {
        try {
            TrackingInfo info = restClient.get()
                    .uri("/api/v1/tracking/{orderId}/latest", orderId)
                    .retrieve()
                    .body(TrackingInfo.class);
            return Optional.ofNullable(info);
        } catch (RestClientException ex) {
            log.warn("Failed to fetch latest tracking for orderId={}: {}", orderId, ex.getMessage());
            return Optional.empty();
        }
    }

    private record TrackingEntryRequest(UUID orderId, String status, String location, String note) {
    }
}
