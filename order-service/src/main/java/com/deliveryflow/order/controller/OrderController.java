package com.deliveryflow.order.controller;

import com.deliveryflow.order.dto.CreateOrderRequest;
import com.deliveryflow.order.dto.OrderResponse;
import com.deliveryflow.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request,
                                     @RequestHeader("X-Customer-Id") UUID customerId) {
        return orderService.createOrder(request, customerId);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId,
                                  @RequestHeader("X-Customer-Id") UUID customerId) {
        return orderService.getOrder(orderId, customerId);
    }

    @GetMapping
    public Page<OrderResponse> getOrderHistory(@RequestHeader("X-Customer-Id") UUID customerId,
                                               Pageable pageable) {
        return orderService.getOrderHistory(customerId, pageable);
    }

    @DeleteMapping("/{orderId}")
    public OrderResponse cancelOrder(@PathVariable UUID orderId,
                                     @RequestHeader("X-Customer-Id") UUID customerId) {
        return orderService.cancelOrder(orderId, customerId);
    }
}
