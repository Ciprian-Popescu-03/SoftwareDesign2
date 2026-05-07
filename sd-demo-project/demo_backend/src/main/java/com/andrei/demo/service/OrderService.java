package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Order;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Product;
import com.andrei.demo.repository.OrderRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PersonRepository personRepository;
    private final ProductRepository productRepository;

    // Manual constructor replacing Lombok
    public OrderService(OrderRepository orderRepository, PersonRepository personRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.personRepository = personRepository;
        this.productRepository = productRepository;
    }

    public Order createOrder(UUID personId, List<UUID> productIds) throws ValidationException {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ValidationException("Cannot create order: Person with ID " + personId + " not found."));

        if (productIds == null || productIds.isEmpty()) {
            throw new ValidationException("Cannot create order: An order must contain at least one product.");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ValidationException("Cannot create order: One or more requested products do not exist in the database.");
        }

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setPerson(person);
        order.setProducts(products);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID id) throws ValidationException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Order with ID " + id + " not found."));
    }

    // NEW: Full Update (PUT)
    public Order updateOrder(UUID id, UUID personId, List<UUID> productIds) throws ValidationException {
        Order existingOrder = getOrderById(id);

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ValidationException("Cannot update order: Person with ID " + personId + " not found."));

        if (productIds == null || productIds.isEmpty()) {
            throw new ValidationException("Cannot update order: An order must contain at least one product.");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ValidationException("Cannot update order: One or more requested products do not exist.");
        }

        existingOrder.setPerson(person);
        existingOrder.setProducts(products);

        return orderRepository.save(existingOrder);
    }

    // NEW: Partial Update (PATCH)
    public Order patchOrder(UUID id, Map<String, Object> updates) throws ValidationException {
        Order existingOrder = getOrderById(id);

        // Update Person if provided
        if (updates.containsKey("personId")) {
            UUID personId = UUID.fromString(updates.get("personId").toString());
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new ValidationException("Cannot patch order: Person with ID " + personId + " not found."));
            existingOrder.setPerson(person);
        }

        // Update Products if provided
        if (updates.containsKey("productIds")) {
            List<String> productIdStrings = (List<String>) updates.get("productIds");
            if (productIdStrings == null || productIdStrings.isEmpty()) {
                throw new ValidationException("Cannot patch order: Product list cannot be empty.");
            }

            List<UUID> productIds = productIdStrings.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            List<Product> products = productRepository.findAllById(productIds);
            if (products.size() != productIds.size()) {
                throw new ValidationException("Cannot patch order: One or more requested products do not exist.");
            }
            existingOrder.setProducts(products);
        }

        return orderRepository.save(existingOrder);
    }

    public void deleteOrder(UUID id) throws ValidationException {
        if (!orderRepository.existsById(id)) {
            throw new ValidationException("Cannot delete: Order with ID " + id + " does not exist.");
        }
        orderRepository.deleteById(id);
    }

    public List<Order> findByPersonId(String personId) {
        // Convert the String from the URL to a UUID for the database
        return orderRepository.findByPersonId(UUID.fromString(personId));
    }

    public Order addProductToOrder(UUID orderId, UUID productId) throws ValidationException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found"));

        order.getProducts().add(product); // Add the product to the order's list
        return orderRepository.save(order); // Save the updated order
    }
}