package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.OrderRepository;
import com.andrei.demo.repository.PersonRepository;
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
public class PersonControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private PersonRepository personRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrderRepository orderRepository;

    private String adminToken;
    private String customerToken;
    private Person adminPerson;
    private Person customerPerson;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        personRepository.deleteAll();

        // Create admin
        adminPerson = new Person();
        adminPerson.setName("Admin User");
        adminPerson.setEmail("admin@test.com");
        adminPerson.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminPerson.setAge(30);
        adminPerson.setRole(Role.ADMIN);
        personRepository.save(adminPerson);

        // Create customer
        customerPerson = new Person();
        customerPerson.setName("Customer User");
        customerPerson.setEmail("customer@test.com");
        customerPerson.setPassword(passwordEncoder.encode("CustomerPass123!"));
        customerPerson.setAge(25);
        customerPerson.setRole(Role.CUSTOMER);
        personRepository.save(customerPerson);

        // Get admin token
        adminToken = getToken("admin@test.com", "AdminPass123!");

        // Get customer token
        customerToken = getToken("customer@test.com", "CustomerPass123!");
    }

    private String getToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        MvcResult result = mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    // --- Registration (public endpoint) ---

    @Test
    void shouldCreatePersonWithoutToken() throws Exception {
        String personJson = "{\"name\":\"New User\", \"email\":\"new@test.com\", \"password\":\"StrongPass123!\", \"age\":22}";

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldHashPasswordOnRegistration() throws Exception {
        String personJson = "{\"name\":\"Hash Test\", \"email\":\"hash@test.com\", \"password\":\"StrongPass123!\", \"age\":22}";

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personJson))
                .andExpect(status().isOk());

        Person saved = personRepository.findByEmail("hash@test.com").orElseThrow();
        assertNotEquals("StrongPass123!", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2a$"));
    }

    @Test
    void shouldRejectRegistrationWithWeakPassword() throws Exception {
        String personJson = "{\"name\":\"Weak Pass\", \"email\":\"weak@test.com\", \"password\":\"123\", \"age\":22}";

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithMissingName() throws Exception {
        String personJson = "{\"email\":\"noname@test.com\", \"password\":\"StrongPass123!\", \"age\":22}";

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAssignCustomerRoleByDefault() throws Exception {
        String personJson = "{\"name\":\"Default Role\", \"email\":\"role@test.com\", \"password\":\"StrongPass123!\", \"age\":22}";

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    // --- Login ---

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        String loginJson = "{\"email\":\"admin@test.com\",\"password\":\"AdminPass123!\"}";

        mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.person.email").value("admin@test.com"));
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        String loginJson = "{\"email\":\"admin@test.com\",\"password\":\"WrongPassword!\"}";

        mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldRejectLoginWithNonExistentEmail() throws Exception {
        String loginJson = "{\"email\":\"nobody@test.com\",\"password\":\"AdminPass123!\"}";

        mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminTokenShouldContainAdminRole() throws Exception {
        String loginJson = "{\"email\":\"admin@test.com\",\"password\":\"AdminPass123!\"}";

        mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.role").value("ADMIN"));
    }

    @Test
    void customerTokenShouldContainCustomerRole() throws Exception {
        String loginJson = "{\"email\":\"customer@test.com\",\"password\":\"CustomerPass123!\"}";

        mockMvc.perform(post("/person/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.role").value("CUSTOMER"));
    }

    // --- JWT Protection ---

    @Test
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/person"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenCustomerAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200WhenAdminAccessesPersonList() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithExpiredToken() throws Exception {
        // A manually crafted expired token
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9.invalid";

        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // --- Admin CRUD operations ---

    @Test
    void adminShouldGetPersonById() throws Exception {
        mockMvc.perform(get("/person/" + adminPerson.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void adminShouldDeletePerson() throws Exception {
        // Create a person to delete
        Person toDelete = new Person();
        toDelete.setName("To Delete");
        toDelete.setEmail("delete@test.com");
        toDelete.setPassword(passwordEncoder.encode("StrongPass123!"));
        toDelete.setAge(20);
        toDelete.setRole(Role.CUSTOMER);
        toDelete = personRepository.save(toDelete);

        mockMvc.perform(delete("/person/" + toDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(personRepository.existsById(toDelete.getId()));
    }

    @Test
    void customerShouldNotDeletePerson() throws Exception {
        mockMvc.perform(delete("/person/" + adminPerson.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldPatchPersonRole() throws Exception {
        mockMvc.perform(patch("/person/" + customerPerson.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void customerShouldNotPatchPerson() throws Exception {
        mockMvc.perform(patch("/person/" + customerPerson.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacker\"}"))
                .andExpect(status().isForbidden());
    }

    // --- Password Reset ---

    @Test
    void shouldRequestPasswordResetWithoutToken() throws Exception {
        mockMvc.perform(post("/forgot-password/request")
                        .param("email", "customer@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenRequestingResetForNonExistentEmail() throws Exception {
        mockMvc.perform(post("/forgot-password/request")
                        .param("email", "nobody@test.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectResetWithWrongCode() throws Exception {
        // First request a code
        mockMvc.perform(post("/forgot-password/request")
                .param("email", "customer@test.com"));

        // Then try to reset with wrong code
        mockMvc.perform(post("/forgot-password/reset")
                        .param("email", "customer@test.com")
                        .param("code", "000000")
                        .param("newPassword", "NewPass123!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}