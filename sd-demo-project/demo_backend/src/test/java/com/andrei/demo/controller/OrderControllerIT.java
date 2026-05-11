package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Product;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.OrderRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken;
    private Person adminPerson;
    private Person customerPerson;
    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        personRepository.deleteAll();

        adminPerson = new Person();
        adminPerson.setName("Admin");
        adminPerson.setEmail("admin@test.com");
        adminPerson.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminPerson.setAge(30);
        adminPerson.setRole(Role.ADMIN);
        adminPerson = personRepository.save(adminPerson);

        customerPerson = new Person();
        customerPerson.setName("Customer");
        customerPerson.setEmail("customer@test.com");
        customerPerson.setPassword(passwordEncoder.encode("CustomerPass123!"));
        customerPerson.setAge(25);
        customerPerson.setRole(Role.CUSTOMER);
        customerPerson = personRepository.save(customerPerson);

        testProduct = new Product();
        testProduct.setName("Product A");
        testProduct.setPrice(10.0);
        testProduct = productRepository.save(testProduct);

        testProduct2 = new Product();
        testProduct2.setName("Product B");
        testProduct2.setPrice(20.0);
        testProduct2 = productRepository.save(testProduct2);

        adminToken = getToken("admin@test.com", "AdminPass123!");
        customerToken = getToken("customer@test.com", "CustomerPass123!");
    }

    private String getToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        MvcResult result = mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    // --- JWT Protection ---

    @Test
    void shouldReturn401WhenCreatingOrderWithoutToken() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenGettingOrdersWithoutToken() throws Exception {
        mockMvc.perform(get("/order"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerShouldCreateOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());

        mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)));

        assertEquals(1, orderRepository.findAll().size());
    }

    @Test
    void customerShouldCreateOrderWithMultipleProducts() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\",\"%s\"]}",
                customerPerson.getId(), testProduct.getId(), testProduct2.getId());

        mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)));
    }

    @Test
    void adminShouldCreateOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                adminPerson.getId(), testProduct.getId());

        mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenCreatingOrderWithNoProducts() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[]}",
                customerPerson.getId());

        mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCreatingOrderWithInvalidPersonId() throws Exception {
        String orderJson = String.format("{\"personId\":\"00000000-0000-0000-0000-000000000000\",\"productIds\":[\"%s\"]}",
                testProduct.getId());

        mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());
    }

    // --- Get Orders ---

    @Test
    void adminShouldGetAllOrders() throws Exception {
        // Create an order first
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        mockMvc.perform(post("/order")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson));

        mockMvc.perform(get("/order")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void customerShouldGetTheirOrders() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        mockMvc.perform(post("/order")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson));

        mockMvc.perform(get("/order/person/" + customerPerson.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        MvcResult created = mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        String orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/order/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    // --- Add Product to Order ---

    @Test
    void shouldAddProductToExistingOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        MvcResult created = mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        String orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/order/" + orderId + "/product/" + testProduct2.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)));
    }

    // --- Delete Order ---

    @Test
    void customerShouldDeleteTheirOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        MvcResult created = mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        String orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/order/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    void adminShouldDeleteAnyOrder() throws Exception {
        String orderJson = String.format("{\"personId\":\"%s\",\"productIds\":[\"%s\"]}",
                customerPerson.getId(), testProduct.getId());
        MvcResult created = mockMvc.perform(post("/order")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        String orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/order/" + orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    void shouldReturn400WhenDeletingNonExistentOrder() throws Exception {
        mockMvc.perform(delete("/order/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}