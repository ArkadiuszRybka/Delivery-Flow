package com.deliveryflow.tracking.controller;

import com.deliveryflow.tracking.dto.CreateTrackingEntryRequest;
import com.deliveryflow.tracking.dto.TrackingEntryResponse;
import com.deliveryflow.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingEntryResponse create(@RequestBody @Valid CreateTrackingEntryRequest request) {
        return trackingService.createEntry(request);
    }

    @GetMapping("/{orderId}")
    public List<TrackingEntryResponse> getHistory(@PathVariable UUID orderId) {
        return trackingService.getHistory(orderId);
    }

    @GetMapping("/{orderId}/latest")
    public TrackingEntryResponse getLatest(@PathVariable UUID orderId) {
        return trackingService.getLatest(orderId);
    }
}
