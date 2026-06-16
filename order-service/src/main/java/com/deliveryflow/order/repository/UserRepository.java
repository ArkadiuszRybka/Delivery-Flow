package com.deliveryflow.order.repository;

import com.deliveryflow.order.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);
    boolean existsByEmail(String email);
}
