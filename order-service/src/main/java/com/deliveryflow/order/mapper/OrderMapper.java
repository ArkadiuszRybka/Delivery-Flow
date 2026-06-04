package com.deliveryflow.order.mapper;

import com.deliveryflow.order.domain.DeliveryAddress;
import com.deliveryflow.order.domain.Order;
import com.deliveryflow.order.domain.OrderItem;
import com.deliveryflow.order.dto.DeliveryAddressDto;
import com.deliveryflow.order.dto.OrderItemResponse;
import com.deliveryflow.order.dto.OrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);

    DeliveryAddressDto toAddressDto(DeliveryAddress address);
}
