package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.model.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();

    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) {
        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        person.setRole(Role.CUSTOMER);
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

        existingPerson.setPassword(passwordEncoder.encode(person.getPassword()));
        return personRepository.save(existingPerson);
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
                    existingPerson.setPassword(passwordEncoder.encode((String) value));
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


    public void saveResetCodeForUser(String email, String code) throws ValidationException {
        personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No user found with email: " + email));
        resetCodes.put(email, code);
    }

    public boolean verifyResetCode(String email, String code) {
        String savedCode = resetCodes.get(email);
        return savedCode != null && savedCode.equals(code);
    }

    public void updatePassword(String email, String newPasswordRaw) throws ValidationException {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No user found with email: " + email));

        person.setPassword(passwordEncoder.encode(newPasswordRaw));
        personRepository.save(person);

        resetCodes.remove(email);
    }


}