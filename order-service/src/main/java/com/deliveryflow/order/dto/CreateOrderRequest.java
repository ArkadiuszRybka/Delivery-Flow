package com.deliveryflow.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item") @Valid List<OrderItemRequest> items,
        @NotNull(message = "Delivery address must not be null") @Valid DeliveryAddressDto deliveryAddress
) {
}
