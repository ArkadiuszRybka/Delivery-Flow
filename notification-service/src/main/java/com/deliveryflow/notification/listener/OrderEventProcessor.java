package com.deliveryflow.notification.listener;

import com.deliveryflow.notification.domain.ProcessedEvent;
import com.deliveryflow.notification.event.OrderCancelled;
import com.deliveryflow.notification.event.OrderConfirmed;
import com.deliveryflow.notification.event.OrderDelivered;
import com.deliveryflow.notification.event.OrderEvent;
import com.deliveryflow.notification.event.OrderShipped;
import com.deliveryflow.notification.repository.ProcessedEventRepository;
import com.deliveryflow.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
public class OrderEventProcessor {

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationService notificationService;

    public OrderEventProcessor(ProcessedEventRepository processedEventRepository, NotificationService notificationService) {
        this.processedEventRepository = processedEventRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void process(OrderEvent event) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            log.info("Skipping already processed event eventId={}, eventType={}", event.eventId(), event.getClass().getSimpleName());
            return;
        }

        switch (event) {
            case OrderConfirmed e -> notificationService.sendOrderConfirmed(e.aggregateId(), e.customerId());
            case OrderShipped e -> notificationService.sendOrderShipped(e.aggregateId(), e.customerId());
            case OrderDelivered e -> notificationService.sendOrderDelivered(e.aggregateId(), e.customerId());
            case OrderCancelled e -> notificationService.sendOrderCancelled(e.aggregateId(), e.customerId(), e.reason());
        }

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.eventId());
        processedEvent.setProcessedAt(Instant.now());
        processedEventRepository.save(processedEvent);
    }
}
