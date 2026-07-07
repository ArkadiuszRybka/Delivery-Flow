package com.deliveryflow.notification.listener;

import com.deliveryflow.notification.event.OrderConfirmed;
import com.deliveryflow.notification.repository.ProcessedEventRepository;
import com.deliveryflow.notification.service.NotificationService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class NotificationConsumerIT {

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
    private ProcessedEventRepository processedEventRepository;

    @MockitoSpyBean
    private NotificationService notificationService;

    @Test
    void consumesOrderConfirmedEventAndRecordsIdempotencyKey() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        publishOrderConfirmed(eventId, orderId, customerId);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(processedEventRepository.existsByEventId(eventId)).isTrue());

        verify(notificationService, times(1)).sendOrderConfirmed(orderId, customerId);
    }

    @Test
    void duplicateEventIsProcessedOnlyOnce() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        publishOrderConfirmed(eventId, orderId, customerId);
        publishOrderConfirmed(eventId, orderId, customerId);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(processedEventRepository.existsByEventId(eventId)).isTrue());

        verify(notificationService, times(1)).sendOrderConfirmed(orderId, customerId);
    }

    private void publishOrderConfirmed(UUID eventId, UUID orderId, UUID customerId) {
        OrderConfirmed event = new OrderConfirmed(eventId, orderId, Instant.now(), "OrderConfirmed", 1,
                customerId, new BigDecimal("49.99"), "PLN", null);

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        try (var producer = new KafkaProducer<String, OrderConfirmed>(
                props, new StringSerializer(), new JacksonJsonSerializer<>(JsonMapper.builder().build()))) {
            producer.send(new ProducerRecord<>("order.events", orderId.toString(), event));
        }
    }
}
