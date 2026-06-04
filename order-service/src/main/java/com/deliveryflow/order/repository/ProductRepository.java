package com.deliveryflow.order.repository;

import com.deliveryflow.order.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(UUID productId);
    List<Product> findByAvailableTrue();
}
