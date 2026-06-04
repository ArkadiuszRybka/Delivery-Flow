package com.deliveryflow.notification.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events") @Getter @Setter
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID eventId;
    @Column(nullable = false)
    private Instant processedAt;
}
