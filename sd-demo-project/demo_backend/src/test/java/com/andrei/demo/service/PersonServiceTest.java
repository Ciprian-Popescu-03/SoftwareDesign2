package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock private PersonRepository personRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PersonService personService;

    private Person mockPerson;
    private PersonCreateDTO mockPersonDTO;
    private UUID personId;

    @BeforeEach
    void setUp() {
        personId = UUID.randomUUID();

        mockPerson = new Person();
        mockPerson.setId(personId);
        mockPerson.setName("Andrei");
        mockPerson.setEmail("andrei@test.com");
        mockPerson.setPassword("hashed-password");
        mockPerson.setAge(21);
        mockPerson.setRole(Role.CUSTOMER);

        mockPersonDTO = new PersonCreateDTO();
        mockPersonDTO.setName("Andrei");
        mockPersonDTO.setEmail("andrei@test.com");
        mockPersonDTO.setPassword("RawPassword123!");
        mockPersonDTO.setAge(21);

        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
    }

    // --- getPeople ---

    @Test
    void getPeople_ReturnsList() {
        when(personRepository.findAll()).thenReturn(Arrays.asList(mockPerson));

        List<Person> people = personService.getPeople();

        assertEquals(1, people.size());
        assertEquals("Andrei", people.get(0).getName());
        verify(personRepository, times(1)).findAll();
    }

    @Test
    void getPeople_ReturnsEmptyList_WhenNoPeople() {
        when(personRepository.findAll()).thenReturn(Collections.emptyList());

        List<Person> people = personService.getPeople();

        assertTrue(people.isEmpty());
    }

    @Test
    void getPeople_ReturnsMultiplePeople() {
        Person second = new Person();
        second.setId(UUID.randomUUID());
        second.setName("Maria");
        when(personRepository.findAll()).thenReturn(Arrays.asList(mockPerson, second));

        List<Person> people = personService.getPeople();

        assertEquals(2, people.size());
    }

    // --- getPersonById ---

    @Test
    void getPersonById_Success() {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        Person found = personService.getPersonById(personId);

        assertNotNull(found);
        assertEquals(personId, found.getId());
    }

    @Test
    void getPersonById_ThrowsException_WhenNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> personService.getPersonById(personId));
    }

    // --- getPersonByEmail ---

    @Test
    void getPersonByEmail_Success() {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));

        Person found = personService.getPersonByEmail("andrei@test.com");

        assertNotNull(found);
        assertEquals("andrei@test.com", found.getEmail());
    }

    @Test
    void getPersonByEmail_ThrowsException_WhenNotFound() {
        when(personRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> personService.getPersonByEmail("unknown@test.com"));
    }

    // --- addPerson ---

    @Test
    void addPerson_Success() {
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);

        Person created = personService.addPerson(mockPersonDTO);

        assertNotNull(created);
        assertEquals("Andrei", created.getName());
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void addPerson_ShouldHashPassword() {
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Person saved = personService.addPerson(mockPersonDTO);

        assertNotEquals("RawPassword123!", saved.getPassword());
        verify(passwordEncoder, times(1)).encode("RawPassword123!");
    }

    @Test
    void addPerson_ShouldSetCustomerRoleByDefault() {
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Person saved = personService.addPerson(mockPersonDTO);

        assertEquals(Role.CUSTOMER, saved.getRole());
    }

    @Test
    void addPerson_ShouldSetCorrectEmail() {
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Person saved = personService.addPerson(mockPersonDTO);

        assertEquals("andrei@test.com", saved.getEmail());
    }

    // --- updatePerson ---

    @Test
    void updatePerson_Success() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);

        Person updated = personService.updatePerson(personId, mockPerson);

        assertNotNull(updated);
        verify(personRepository, times(1)).save(any(Person.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void updatePerson_ThrowsException_WhenNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> personService.updatePerson(personId, mockPerson));
    }

    // --- deletePerson ---

    @Test
    void deletePerson_Success() {
        doNothing().when(personRepository).deleteById(personId);

        personService.deletePerson(personId);

        verify(personRepository, times(1)).deleteById(personId);
    }

    // --- patchPerson ---

    @Test
    void patchPerson_ShouldUpdateName() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Name");

        Person patched = personService.patchPerson(personId, updates);

        assertEquals("Updated Name", patched.getName());
    }

    @Test
    void patchPerson_ShouldUpdateRole() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("role", "ADMIN");

        Person patched = personService.patchPerson(personId, updates);

        assertEquals(Role.ADMIN, patched.getRole());
    }

    @Test
    void patchPerson_ShouldHashPassword() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("password", "NewPass123!");

        personService.patchPerson(personId, updates);

        verify(passwordEncoder, times(1)).encode("NewPass123!");
    }

    @Test
    void patchPerson_ThrowsException_WhenNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> personService.patchPerson(personId, new HashMap<>()));
    }

    // --- Password Reset ---

    @Test
    void saveResetCode_ThrowsException_WhenEmailNotFound() {
        when(personRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> personService.saveResetCodeForUser("nobody@test.com", "123456"));
    }

    @Test
    void saveResetCode_Success() throws ValidationException {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));

        personService.saveResetCodeForUser("andrei@test.com", "123456");

        assertTrue(personService.verifyResetCode("andrei@test.com", "123456"));
    }

    @Test
    void verifyResetCode_ReturnsFalse_WhenCodeWrong() throws ValidationException {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));
        personService.saveResetCodeForUser("andrei@test.com", "123456");

        assertFalse(personService.verifyResetCode("andrei@test.com", "000000"));
    }

    @Test
    void updatePassword_ShouldHashNewPassword() throws ValidationException {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);

        personService.updatePassword("andrei@test.com", "NewPass123!");

        verify(passwordEncoder, times(1)).encode("NewPass123!");
    }

    @Test
    void updatePassword_ShouldRemoveCodeAfterReset() throws ValidationException {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);
        personService.saveResetCodeForUser("andrei@test.com", "123456");

        personService.updatePassword("andrei@test.com", "NewPass123!");

        assertFalse(personService.verifyResetCode("andrei@test.com", "123456"));
    }

    @Test
    void updatePassword_ThrowsException_WhenEmailNotFound() {
        when(personRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> personService.updatePassword("nobody@test.com", "NewPass123!"));
    }
}