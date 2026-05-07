package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Product;
import com.andrei.demo.repository.OrderRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PersonRepository personRepository;
    @Autowired private ProductRepository productRepository;

    private Person testPerson;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        personRepository.deleteAll();
        productRepository.deleteAll();

        testPerson = new Person();
        testPerson.setName("Order Tester");
        testPerson.setEmail("order@test.com");
        testPerson.setPassword("pass");
        testPerson = personRepository.save(testPerson);

        testProduct = new Product();
        testProduct.setName("Order Item");
        testProduct.setPrice(10.0);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void shouldCreateOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\", \"productIds\":[\"%s\"]}",
                testPerson.getId(), testProduct.getId());

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk());

        assertEquals(1, orderRepository.findAll().size());
    }
}