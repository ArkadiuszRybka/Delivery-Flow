package com.deliveryflow.tracking.repository;

import com.deliveryflow.tracking.domain.TrackingEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TrackingEntryRepository extends JpaRepository<TrackingEntry, Long> {
    List<TrackingEntry> findByOrderIdOrderByRecordedAtAsc(UUID orderId);
    Optional<TrackingEntry> findTopByOrderIdOrderByRecordedAtDesc(UUID orderId);
}
