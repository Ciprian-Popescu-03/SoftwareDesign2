package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Product;
import com.andrei.demo.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@CrossOrigin
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) throws ValidationException {
        return productService.getProductById(id);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody Product product) throws ValidationException {
        return productService.updateProduct(id, product);
    }

    @PatchMapping("/{id}")
    public Product patchProduct(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return productService.patchProduct(id, updates);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) throws ValidationException {
        productService.deleteProduct(id);
    }
}