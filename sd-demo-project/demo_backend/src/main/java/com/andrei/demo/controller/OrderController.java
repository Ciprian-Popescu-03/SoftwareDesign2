package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Order;
import com.andrei.demo.model.OrderCreateDTO;
import com.andrei.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable UUID id) throws ValidationException {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public Order createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) throws ValidationException {
        return orderService.createOrder(orderCreateDTO.getPersonId(), orderCreateDTO.getProductIds());
    }

    @PatchMapping("/{id}")
    public Order patchOrder(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return orderService.patchOrder(id, updates);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable UUID id) throws ValidationException {
        orderService.deleteOrder(id);
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<Order>> getOrdersByPerson(@PathVariable String personId) {
        // Make sure your repository is finding orders where person.id = personId
        return ResponseEntity.ok(orderService.findByPersonId(personId));
    }

    @PostMapping("/{orderId}/product/{productId}")
    public Order addProductToOrder(@PathVariable UUID orderId, @PathVariable UUID productId) throws ValidationException {
        // NOTE: If your OrderService method has a slightly different name
        // (like 'addToOrder' or 'addProduct'), just update the name below!
        return orderService.addProductToOrder(orderId, productId);
    }
}