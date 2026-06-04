package com.deliveryflow.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable @Getter @Setter
public class DeliveryAddress {
    @Column(nullable = false)
    private String street;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false, length = 20)
    private String postalCode;
    @Column(nullable = false, length = 100)
    private String country;
}
