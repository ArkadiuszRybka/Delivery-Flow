package com.deliveryflow.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "order.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAfterCommit(OrderEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }

    private void publish(OrderEvent event) {
        kafkaTemplate.send(TOPIC, event.aggregateId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event eventId={}, eventType={}, orderId={}: {}",
                                event.eventId(), event.eventType(), event.aggregateId(), ex.getMessage(), ex);
                    } else {
                        log.info("Published event eventId={}, eventType={}, orderId={}",
                                event.eventId(), event.eventType(), event.aggregateId());
                    }
                });
    }
}
