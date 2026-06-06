package com.deliveryflow.order.scheduler;

import com.deliveryflow.order.domain.CancellationReason;
import com.deliveryflow.order.domain.OrderStatus;
import com.deliveryflow.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Slf4j
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;

    public OrderExpirationScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expireOrders() {
        var expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.PENDING, Instant.now());

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} expired orders to cancel", expiredOrders.size());

        for (var order : expiredOrders) {
            order.cancel(CancellationReason.EXPIRED);
            log.info("Expired order orderId={}", order.getOrderId());
        }
    }
}
