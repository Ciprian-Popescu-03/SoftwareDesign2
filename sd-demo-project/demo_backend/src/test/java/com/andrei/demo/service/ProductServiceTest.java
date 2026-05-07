package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Product;
import com.andrei.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Test Laptop");
        mockProduct.setPrice(1200.00);
    }

    @Test
    void getAllProducts_ReturnsList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(mockProduct));

        // Now uses your actual method name!
        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Test Laptop", products.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_Success() throws ValidationException {
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        Product foundProduct = productService.getProductById(productId);

        assertNotNull(foundProduct);
        assertEquals(productId, foundProduct.getId());
    }

    @Test
    void getProductById_ThrowsException_WhenNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            productService.getProductById(productId);
        });
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product createdProduct = productService.createProduct(mockProduct);

        assertNotNull(createdProduct);
        assertEquals("Test Laptop", createdProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() throws ValidationException {
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_ThrowsException_WhenNotFound() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ValidationException.class, () -> {
            productService.deleteProduct(productId);
        });
    }
}