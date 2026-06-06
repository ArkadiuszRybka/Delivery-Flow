package com.deliveryflow.order.controller;

import com.deliveryflow.order.dto.ProductResponse;
import com.deliveryflow.order.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return productService.getAvailableProducts();
    }

    @GetMapping("/{productId}")
    public ProductResponse getOneProduct(@PathVariable UUID productId){
        return productService.getProduct(productId);
    }
}
