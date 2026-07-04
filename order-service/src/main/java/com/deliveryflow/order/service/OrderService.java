package com.deliveryflow.order.service;

import com.deliveryflow.order.client.TrackingClient;
import com.deliveryflow.order.domain.*;
import com.deliveryflow.order.dto.CreateOrderRequest;
import com.deliveryflow.order.dto.OrderResponse;
import com.deliveryflow.order.dto.TrackingInfo;
import com.deliveryflow.order.event.OrderCancelled;
import com.deliveryflow.order.event.OrderConfirmed;
import com.deliveryflow.order.event.OrderDelivered;
import com.deliveryflow.order.event.OrderEventPublisher;
import com.deliveryflow.order.event.OrderShipped;
import com.deliveryflow.order.exception.OrderNotFoundException;
import com.deliveryflow.order.exception.PaymentIntentCreationException;
import com.deliveryflow.order.exception.ProductNotFoundException;
import com.deliveryflow.order.mapper.OrderMapper;
import com.deliveryflow.order.repository.OrderRepository;
import com.deliveryflow.order.repository.ProductRepository;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final PaymentService paymentService;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderMapper orderMapper,
                        TrackingClient trackingClient,
                        PaymentService paymentService,
                        OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.trackingClient = trackingClient;
        this.paymentService = paymentService;
        this.orderEventPublisher = orderEventPublisher;
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

        try {
            var paymentIntent = paymentService.createPaymentIntent(saved.getOrderId(), saved.getTotalAmount(), saved.getCurrency());
            saved.setPaymentIntentId(paymentIntent.getId());
        } catch (StripeException e) {
            throw new PaymentIntentCreationException(saved.getOrderId(), e);
        }

        trackingClient.createTrackingEntry(saved.getOrderId(), "PENDING");
        log.info("Created order orderId={}, customerId={}", saved.getOrderId(), customerId);
        return orderMapper.toResponse(saved);
    }

    @Cacheable(value = "orders", key = "#orderId + '_' + #customerId")
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderNotFoundException(orderId);
        }
        TrackingInfo tracking = trackingClient.getLatestTracking(orderId).orElse(null);
        return orderMapper.toResponse(order, tracking);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrderHistory(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderNotFoundException(orderId);
        }
        order.cancel(CancellationReason.CUSTOMER_REQUEST);
        trackingClient.createTrackingEntry(orderId, "CANCELLED");
        orderEventPublisher.publishAfterCommit(
                OrderCancelled.of(orderId, order.getCustomerId(), CancellationReason.CUSTOMER_REQUEST));
        log.info("Cancelled order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderResponse confirmOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("Order already confirmed, ignoring duplicate confirmation orderId={}", orderId);
            return orderMapper.toResponse(order);
        }
        order.confirm();
        trackingClient.createTrackingEntry(orderId, "CONFIRMED");
        orderEventPublisher.publishAfterCommit(OrderConfirmed.of(orderId, order.getCustomerId(),
                order.getTotalAmount(), order.getCurrency(), orderMapper.toAddressDto(order.getDeliveryAddress())));
        log.info("Confirmed order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderResponse cancelOrderForPaymentFailure(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order already cancelled, ignoring duplicate payment failure orderId={}", orderId);
            return orderMapper.toResponse(order);
        }
        order.cancel(CancellationReason.PAYMENT_FAILED);
        trackingClient.createTrackingEntry(orderId, "CANCELLED");
        orderEventPublisher.publishAfterCommit(
                OrderCancelled.of(orderId, order.getCustomerId(), CancellationReason.PAYMENT_FAILED));
        log.info("Cancelled order due to payment failure orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderResponse shipOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.ship();
        trackingClient.createTrackingEntry(orderId, "SHIPPED");
        orderEventPublisher.publishAfterCommit(OrderShipped.of(orderId, order.getCustomerId(), null));
        log.info("Shipped order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderResponse deliverOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.deliver();
        trackingClient.createTrackingEntry(orderId, "DELIVERED");
        orderEventPublisher.publishAfterCommit(OrderDelivered.of(orderId, order.getCustomerId()));
        log.info("Delivered order orderId={}", orderId);
        return orderMapper.toResponse(order);
    }
}
