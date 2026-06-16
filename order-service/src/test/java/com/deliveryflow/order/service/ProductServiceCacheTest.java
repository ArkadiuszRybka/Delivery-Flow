package com.deliveryflow.order.service;

import com.deliveryflow.order.domain.Product;
import com.deliveryflow.order.dto.CreateProductRequest;
import com.deliveryflow.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProductServiceCacheTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ProductRepository productRepository;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void getAvailableProducts_secondCallIsServedFromCache() {
        when(productRepository.findByAvailableTrue()).thenReturn(List.of(sampleProduct()));

        productService.getAvailableProducts();
        productService.getAvailableProducts();

        verify(productRepository, times(1)).findByAvailableTrue();
    }

    @Test
    void createProduct_evictsProductsCache() throws InterruptedException {
        when(productRepository.findByAvailableTrue()).thenReturn(List.of(sampleProduct()));
        when(productRepository.save(any())).thenReturn(sampleProduct());

        productService.getAvailableProducts();
        productService.createProduct(new CreateProductRequest("New Product", "desc", BigDecimal.TEN, "PLN"));
        Thread.sleep(50);
        productService.getAvailableProducts();

        verify(productRepository, times(2)).findByAvailableTrue();
    }

    private Product sampleProduct() {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("desc");
        product.setPrice(BigDecimal.valueOf(9.99));
        product.setCurrency("PLN");
        product.setAvailable(true);
        return product;
    }
}
