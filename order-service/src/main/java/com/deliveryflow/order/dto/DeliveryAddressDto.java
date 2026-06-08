package com.deliveryflow.order.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryAddressDto(
        @NotBlank(message = "Street must not be blank") String street,
        @NotBlank(message = "City must not be blank") String city,
        @NotBlank(message = "Postal code must not be blank") String postalCode,
        @NotBlank(message = "Country must not be blank") String country
) {
}
