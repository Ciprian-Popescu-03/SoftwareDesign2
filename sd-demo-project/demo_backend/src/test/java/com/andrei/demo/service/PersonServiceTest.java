package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    // We must mock PasswordEncoder because PersonService uses it now!
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonService personService;

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
        mockPerson.setPassword("password123");
        mockPerson.setAge(21);

        mockPersonDTO = new PersonCreateDTO();
        mockPersonDTO.setName("Andrei");
        mockPersonDTO.setEmail("andrei@test.com");
        mockPersonDTO.setPassword("password123");
        mockPersonDTO.setAge(21);

        // Tell Mockito to pretend to encode passwords whenever requested
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
    }

    @Test
    void getPeople_ReturnsList() {
        when(personRepository.findAll()).thenReturn(Arrays.asList(mockPerson));

        List<Person> people = personService.getPeople();

        assertEquals(1, people.size());
        assertEquals("Andrei", people.get(0).getName());
        verify(personRepository, times(1)).findAll();
    }

    @Test
    void getPersonById_Success() {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        Person foundPerson = personService.getPersonById(personId);

        assertNotNull(foundPerson);
        assertEquals(personId, foundPerson.getId());
    }

    @Test
    void getPersonById_ThrowsIllegalStateException_WhenNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            personService.getPersonById(personId);
        });
    }

    @Test
    void getPersonByEmail_Success() {
        when(personRepository.findByEmail("andrei@test.com")).thenReturn(Optional.of(mockPerson));

        Person foundPerson = personService.getPersonByEmail("andrei@test.com");

        assertNotNull(foundPerson);
        assertEquals("andrei@test.com", foundPerson.getEmail());
    }

    @Test
    void getPersonByEmail_ThrowsIllegalStateException_WhenNotFound() {
        when(personRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            personService.getPersonByEmail("unknown@test.com");
        });
    }

    @Test
    void addPerson_Success() {
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);

        Person createdPerson = personService.addPerson(mockPersonDTO);

        assertNotNull(createdPerson);
        assertEquals("Andrei", createdPerson.getName());
        verify(personRepository, times(1)).save(any(Person.class));
        // Verify that the password encoder was called during creation
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void updatePerson_Success() throws ValidationException {
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);

        Person updatedPerson = personService.updatePerson(personId, mockPerson);

        assertNotNull(updatedPerson);
        verify(personRepository, times(1)).save(any(Person.class));
        // Verify that the password encoder was called during update
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void updatePerson_ThrowsValidationException_WhenNotFound() {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            personService.updatePerson(personId, mockPerson);
        });
    }

    @Test
    void deletePerson_Success() {
        doNothing().when(personRepository).deleteById(personId);

        personService.deletePerson(personId);

        verify(personRepository, times(1)).deleteById(personId);
    }

    @Test
    void addPerson_ShouldHashPassword() {
        // Arrange
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Person savedPerson = personService.addPerson(mockPersonDTO);

        // Assert
        // Check that the password is NOT the raw string "password123"
        assertNotEquals("password123", savedPerson.getPassword());

        // Verify the encoder was actually used
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // Note: The login tests have been removed because PersonService no longer handles login logic!
}