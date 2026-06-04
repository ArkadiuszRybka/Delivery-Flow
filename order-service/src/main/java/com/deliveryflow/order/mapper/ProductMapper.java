package com.deliveryflow.order.mapper;

import com.deliveryflow.order.domain.Product;
import com.deliveryflow.order.dto.CreateProductRequest;
import com.deliveryflow.order.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "available", expression = "java(true)")
    Product toEntity(CreateProductRequest request);
}
