package com.deliveryflow.notification.event;

public record DeliveryAddress(
        String street,
        String city,
        String postalCode,
        String country
) {
}
