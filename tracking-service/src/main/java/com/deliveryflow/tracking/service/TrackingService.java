package com.deliveryflow.tracking.service;

import com.deliveryflow.tracking.dto.CreateTrackingEntryRequest;
import com.deliveryflow.tracking.dto.TrackingEntryResponse;
import com.deliveryflow.tracking.exception.TrackingNotFoundException;
import com.deliveryflow.tracking.mapper.TrackingEntryMapper;
import com.deliveryflow.tracking.repository.TrackingEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TrackingService {

    private final TrackingEntryRepository repository;
    private final TrackingEntryMapper mapper;

    public TrackingService(TrackingEntryRepository repository, TrackingEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public TrackingEntryResponse createEntry(CreateTrackingEntryRequest request) {
        var entry = mapper.toEntity(request);
        var saved = repository.save(entry);
        log.info("Created tracking entry for orderId={}, status={}", request.orderId(), request.status());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TrackingEntryResponse> getHistory(UUID orderId) {
        return repository.findByOrderIdOrderByRecordedAtAsc(orderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrackingEntryResponse getLatest(UUID orderId) {
        return repository.findTopByOrderIdOrderByRecordedAtDesc(orderId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new TrackingNotFoundException(orderId));
    }
}
