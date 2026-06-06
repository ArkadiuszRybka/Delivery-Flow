package com.deliveryflow.order.service;

import com.deliveryflow.order.domain.Product;
import com.deliveryflow.order.dto.CreateProductRequest;
import com.deliveryflow.order.dto.ProductResponse;
import com.deliveryflow.order.exception.ProductNotFoundException;
import com.deliveryflow.order.mapper.ProductMapper;
import com.deliveryflow.order.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByAvailableTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        return productRepository.findByProductId(productId)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        var product = productMapper.toEntity(request);
        product.setProductId(UUID.randomUUID());
        var saved = productRepository.save(product);
        log.info("Created product productId={}, name={}", saved.getProductId(), saved.getName());
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, CreateProductRequest request) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCurrency(request.currency());

        log.info("Updated product productId={}", productId);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setAvailable(false);
        log.info("Deactivated product productId={}", productId);
    }
}
