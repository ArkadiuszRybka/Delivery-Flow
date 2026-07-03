package com.deliveryflow.order.mapper;

import com.deliveryflow.order.domain.DeliveryAddress;
import com.deliveryflow.order.domain.Order;
import com.deliveryflow.order.domain.OrderItem;
import com.deliveryflow.order.dto.DeliveryAddressDto;
import com.deliveryflow.order.dto.OrderItemResponse;
import com.deliveryflow.order.dto.OrderResponse;
import com.deliveryflow.order.dto.TrackingInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "tracking", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderId", source = "order.orderId")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "tracking", source = "tracking")
    OrderResponse toResponse(Order order, TrackingInfo tracking);

    OrderItemResponse toItemResponse(OrderItem item);

    DeliveryAddressDto toAddressDto(DeliveryAddress address);

    DeliveryAddress toAddress(DeliveryAddressDto dto);
}
