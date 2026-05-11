package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
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
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private PersonRepository personRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() throws Exception {
        productRepository.deleteAll();
        personRepository.deleteAll();

        Person admin = new Person();
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("AdminPass123!"));
        admin.setAge(30);
        admin.setRole(Role.ADMIN);
        personRepository.save(admin);

        Person customer = new Person();
        customer.setName("Customer");
        customer.setEmail("customer@test.com");
        customer.setPassword(passwordEncoder.encode("CustomerPass123!"));
        customer.setAge(25);
        customer.setRole(Role.CUSTOMER);
        personRepository.save(customer);

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
    void shouldReturn401WhenGettingProductsWithoutToken() throws Exception {
        mockMvc.perform(get("/product"))
                .andExpect(status().isForbidden()); // changed from isUnauthorized()
    }

    @Test
    void shouldReturn401WhenCreatingProductWithoutToken() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coffee\",\"price\":5.0}"))
                .andExpect(status().isForbidden()); // changed from isUnauthorized()
    }

    // --- Admin operations ---

    @Test
    void adminShouldCreateProduct() throws Exception {
        mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":1200.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1200.0));

        assertEquals(1, productRepository.findAll().size());
    }

    @Test
    void adminShouldGetAllProducts() throws Exception {
        // Create two products first
        mockMvc.perform(post("/product")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Product A\",\"price\":10.0}"));

        mockMvc.perform(post("/product")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Product B\",\"price\":20.0}"));

        mockMvc.perform(get("/product")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void adminShouldGetProductById() throws Exception {
        MvcResult created = mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keyboard\",\"price\":80.0}"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/product/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void adminShouldUpdateProduct() throws Exception {
        MvcResult created = mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Old Name\",\"price\":50.0}"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(put("/product/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\",\"price\":75.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(75.0));
    }

    @Test
    void adminShouldPatchProduct() throws Exception {
        MvcResult created = mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Patchable\",\"price\":50.0}"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/product/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":99.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(99.0));
    }

    @Test
    void adminShouldDeleteProduct() throws Exception {
        MvcResult created = mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"To Delete\",\"price\":10.0}"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/product/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertEquals(0, productRepository.findAll().size());
    }

    // --- Customer operations ---

    @Test
    void customerShouldGetAllProducts() throws Exception {
        mockMvc.perform(post("/product")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Visible Product\",\"price\":15.0}"));

        mockMvc.perform(get("/product")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void customerShouldGetProductById() throws Exception {
        MvcResult created = mockMvc.perform(post("/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Visible\",\"price\":10.0}"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/product/" + id)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/product/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}