package com.deliveryflow.tracking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracking_entries") @Getter @Setter
public class TrackingEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID trackingId;
    @Column(nullable = false)
    private UUID orderId;
    @Column(nullable = false, length = 50)
    private String status;
    @Column(length = 255)
    private String location;
    @Column(length = 1000)
    private String note;
    @Column(nullable = false)
    private Instant recordedAt;
}
