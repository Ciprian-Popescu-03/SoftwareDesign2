package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import com.andrei.demo.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) {
        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(personDTO.getPassword());
        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) throws ValidationException{
        Optional<Person> personOptional = personRepository.findById(uuid);
        if(personOptional.isEmpty()) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }
        Person existingPerson = personOptional.get();
        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setPassword(person.getPassword());
        return personRepository.save(existingPerson);
    }

    public Person updatePerson2(UUID uuid, Person person) throws ValidationException{
        return personRepository
                .findById(uuid)
                .map(existingPerson -> {
                    existingPerson.setName(person.getName());
                    existingPerson.setAge(person.getAge());
                    existingPerson.setEmail(person.getEmail());
                    existingPerson.setPassword(person.getPassword());
                    return personRepository.save(existingPerson);
                })
                .orElseThrow(
                        () -> new ValidationException("Person with id " + uuid + " not found")
                );
    }

    public void deletePerson(UUID uuid) {
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }

    public Person patchPerson(UUID uuid, Map<String, Object> updates) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    existingPerson.setName((String) value);
                    break;
                case "email":
                    existingPerson.setEmail((String) value);
                    break;
                case "password":
                    existingPerson.setPassword((String) value);
                    break;
                case "age":
                    existingPerson.setAge((Integer) value);
                    break;
                case "role":
                    existingPerson.setRole(Role.valueOf(value.toString().toUpperCase()));
                    break;
            }
        });
        return personRepository.save(existingPerson);
    }

    // Add this method inside PersonService.java
    public Person login(String email, String password) throws ValidationException {
        // 1. Find the person by email
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid email or password."));

        // 2. Check if the password matches (In a real app this would use BCrypt, but raw strings are fine for this assignment)
        if (!person.getPassword().equals(password)) {
            throw new ValidationException("Invalid email or password.");
        }

        // 3. Return the person so the frontend can see their Role (ADMIN or CUSTOMER)
        return person;
    }
}