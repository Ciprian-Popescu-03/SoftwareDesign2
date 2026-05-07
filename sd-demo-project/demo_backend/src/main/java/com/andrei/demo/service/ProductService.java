package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Product;
import com.andrei.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(UUID id) throws ValidationException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Product with ID " + id + " not found."));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, Product productDetails) throws ValidationException {
        Product existing = getProductById(id);
        existing.setName(productDetails.getName());
        existing.setPrice(productDetails.getPrice());
        return productRepository.save(existing);
    }

    public Product patchProduct(UUID id, Map<String, Object> updates) throws ValidationException {
        Product existingProduct = getProductById(id);

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    existingProduct.setName((String) value);
                    break;
                case "price":
                    // Safely handle if the number comes in as an Integer or Double
                    existingProduct.setPrice(Double.valueOf(value.toString()));
                    break;
            }
        });

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(UUID id) throws ValidationException {
        if (!productRepository.existsById(id)) {
            throw new ValidationException("Cannot delete: Product with ID " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }
}