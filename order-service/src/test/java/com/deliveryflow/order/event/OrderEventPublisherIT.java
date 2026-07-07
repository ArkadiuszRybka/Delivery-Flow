package com.deliveryflow.order.event;

import com.deliveryflow.order.domain.DeliveryAddress;
import com.deliveryflow.order.domain.Order;
import com.deliveryflow.order.domain.OrderStatus;
import com.deliveryflow.order.repository.OrderRepository;
import com.deliveryflow.order.service.OrderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderEventPublisherIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1")).withKraft();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void confirmOrder_publishesOrderConfirmedEventWithOrderIdAsKey() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        orderRepository.save(pendingOrder(orderId, customerId));

        try (Consumer<String, OrderConfirmed> consumer = createConsumer(OrderConfirmed.class)) {
            consumer.subscribe(List.of("order.events"));

            orderService.confirmOrder(orderId);

            ConsumerRecord<String, OrderConfirmed> record = findRecordForOrder(consumer, orderId);
            assertThat(record.key()).isEqualTo(orderId.toString());
            assertThat(record.value().eventType()).isEqualTo("OrderConfirmed");
            assertThat(record.value().aggregateId()).isEqualTo(orderId);
            assertThat(record.value().customerId()).isEqualTo(customerId);
            assertThat(record.value().totalAmount()).isEqualByComparingTo("49.99");
        }
    }

    @Test
    void cancelOrder_publishesOrderCancelledEventWithCustomerRequestReason() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        orderRepository.save(pendingOrder(orderId, customerId));

        try (Consumer<String, OrderCancelled> consumer = createConsumer(OrderCancelled.class)) {
            consumer.subscribe(List.of("order.events"));

            orderService.cancelOrder(orderId, customerId);

            ConsumerRecord<String, OrderCancelled> record = findRecordForOrder(consumer, orderId);
            assertThat(record.key()).isEqualTo(orderId.toString());
            assertThat(record.value().eventType()).isEqualTo("OrderCancelled");
            assertThat(record.value().reason()).isEqualTo("CUSTOMER_REQUEST");
        }
    }

    private <T extends OrderEvent> ConsumerRecord<String, T> findRecordForOrder(Consumer<String, T> consumer, UUID orderId) {
        ConsumerRecords<String, T> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        for (ConsumerRecord<String, T> record : records) {
            if (record.value().aggregateId().equals(orderId)) {
                return record;
            }
        }
        throw new AssertionError("No event found for orderId=" + orderId);
    }

    private Order pendingOrder(UUID orderId, UUID customerId) {
        DeliveryAddress address = new DeliveryAddress();
        address.setStreet("Testowa 1");
        address.setCity("Warszawa");
        address.setPostalCode("00-001");
        address.setCountry("PL");

        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("49.99"));
        order.setCurrency("PLN");
        order.setDeliveryAddress(address);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setExpiresAt(Instant.now().plusSeconds(900));
        return order;
    }

    private <T> Consumer<String, T> createConsumer(Class<T> targetType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        var consumerFactory = new DefaultKafkaConsumerFactory<String, T>(
                props, new StringDeserializer(), new JacksonJsonDeserializer<>(targetType, false));
        return consumerFactory.createConsumer();
    }
}
