package com.deliveryflow.notification.repository;

import com.deliveryflow.notification.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(UUID eventId);
}
