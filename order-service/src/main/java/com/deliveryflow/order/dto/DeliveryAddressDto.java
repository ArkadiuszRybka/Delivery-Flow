package com.deliveryflow.order.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryAddressDto(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country
) {
}
