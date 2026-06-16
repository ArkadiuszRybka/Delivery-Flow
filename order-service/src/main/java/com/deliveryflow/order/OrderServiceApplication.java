package com.deliveryflow.order;

import com.deliveryflow.order.client.TrackingClientProperties;
import com.deliveryflow.order.config.StripeProperties;
import com.deliveryflow.order.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({TrackingClientProperties.class, JwtProperties.class, StripeProperties.class})
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
