package com.deliveryflow.order.exception;

import com.deliveryflow.order.domain.OrderStatus;

import java.util.UUID;

public class OrderStatusException extends RuntimeException{
    public OrderStatusException(UUID orderId, OrderStatus current, OrderStatus target) {super("Cannot transition order " + orderId +
            " from " + current + " to " + target);}
}
