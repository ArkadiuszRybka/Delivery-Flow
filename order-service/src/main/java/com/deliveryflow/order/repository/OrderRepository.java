package com.deliveryflow.order.repository;

import com.deliveryflow.order.domain.Order;
import com.deliveryflow.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(UUID orderId);
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);
    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, Instant time);
}
