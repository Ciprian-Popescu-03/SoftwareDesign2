package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Order;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Product;
import com.andrei.demo.repository.OrderRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Person mockPerson;
    private Product mockProduct;
    private UUID personId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        personId = UUID.randomUUID();
        productId = UUID.randomUUID();

        mockPerson = new Person();
        mockPerson.setId(personId);
        mockPerson.setName("Test User");

        mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Test Product");
        mockProduct.setPrice(10.0);
    }

    @Test
    void createOrder_Success() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(mockProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order createdOrder = orderService.createOrder(personId, Arrays.asList(productId));

        assertNotNull(createdOrder);
        assertEquals(personId, createdOrder.getPerson().getId());
        assertEquals(1, createdOrder.getProducts().size());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void createOrder_ThrowsException_WhenPersonNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            orderService.createOrder(personId, Arrays.asList(productId));
        });
    }

    @Test
    void createOrder_ThrowsException_WhenProductsEmpty() {
        when(personRepository.findById(any())).thenReturn(Optional.of(mockPerson));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            orderService.createOrder(personId, Collections.emptyList());
        });

        assertTrue(exception.getMessage().contains("at least one product"));
    }


}