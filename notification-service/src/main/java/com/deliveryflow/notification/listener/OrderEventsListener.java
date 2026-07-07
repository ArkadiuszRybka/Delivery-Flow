package com.deliveryflow.notification.listener;

import com.deliveryflow.notification.event.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    private final OrderEventProcessor orderEventProcessor;

    public OrderEventsListener(OrderEventProcessor orderEventProcessor) {
        this.orderEventProcessor = orderEventProcessor;
    }

    @KafkaListener(topics = "order.events")
    public void onOrderEvent(OrderEvent event, Acknowledgment acknowledgment) {
        orderEventProcessor.process(event);
        acknowledgment.acknowledge();
    }
}
