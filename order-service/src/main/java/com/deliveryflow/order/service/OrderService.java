package com.deliveryflow.order.service;

import com.deliveryflow.order.client.TrackingClient;
import com.deliveryflow.order.domain.*;
import com.deliveryflow.order.dto.CreateOrderRequest;
import com.deliveryflow.order.dto.OrderResponse;
import com.deliveryflow.order.exception.OrderNotFoundException;
import com.deliveryflow.order.exception.ProductNotFoundException;
import com.deliveryflow.order.mapper.OrderMapper;
import com.deliveryflow.order.repository.OrderRepository;
import com.deliveryflow.order.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final TrackingClient trackingClient;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderMapper orderMapper,
                        TrackingClient trackingClient) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.trackingClient = trackingClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UUID customerId) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(orderMapper.toAddress(request.deliveryAddress()));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));

        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = null;

        for (var itemRequest : request.items()) {
            Product product = productRepository.findByProductId(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.productId()));

            if (!product.isAvailable()) {
                throw new ProductNotFoundException(itemRequest.productId());
            }

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));

            if (currency == null) {
                currency = product.getCurrency();
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getProductId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);
        }

        order.setTotalAmount(totalAmount);
        order.setCurrency(currency);

        Order saved = orderRepository.save(order);
        trackingClient.createTrackingEntry(saved.getOrderId(), "PENDING");
        log.info("Created order orderId={}, customerId={}", saved.getOrderId(), customerId);
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderNotFoundException(orderId);
        }
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrderHistory(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderNotFoundException(orderId);
        }
        order.cancel(CancellationReason.CUSTOMER_REQUEST);
        trackingClient.createTrackingEntry(orderId, "CANCELLED");
        log.info("Cancelled order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.confirm();
        trackingClient.createTrackingEntry(orderId, "CONFIRMED");
        log.info("Confirmed order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.ship();
        trackingClient.createTrackingEntry(orderId, "SHIPPED");
        log.info("Shipped order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.deliver();
        trackingClient.createTrackingEntry(orderId, "DELIVERED");
        log.info("Delivered order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }
}
