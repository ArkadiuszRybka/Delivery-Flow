package com.deliveryflow.order.controller;

import com.deliveryflow.order.dto.OrderResponse;
import com.deliveryflow.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{orderId}/confirm")
    public OrderResponse confirm(@PathVariable UUID orderId) {
        return orderService.confirmOrder(orderId);
    }

    @PutMapping("/{orderId}/ship")
    public OrderResponse ship(@PathVariable UUID orderId) {
        return orderService.shipOrder(orderId);
    }

    @PutMapping("/{orderId}/deliver")
    public OrderResponse deliver(@PathVariable UUID orderId) {
        return orderService.deliverOrder(orderId);
    }
}
