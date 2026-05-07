package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginDTO;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.model.Person;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.UUID;


@RestController
@CrossOrigin
public class PersonController {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/person")
    public List<Person> getPeople() {
        return personService.getPeople();
    }

    @GetMapping("/person/{uuid}")
    public Person getPersonById(@PathVariable UUID uuid) {
        return personService.getPersonById(uuid);
    }

    @GetMapping("/person/email/{email}")
    public Person getPersonByEmail(@PathVariable String email) {
        return personService.getPersonByEmail(email);
    }

    @PostMapping("/person")
    public Person addPerson(
            @Valid @RequestBody PersonCreateDTO personDTO
    ) {
        return personService.addPerson(personDTO);
    }

    @PutMapping("/person/{uuid}")
    public Person updatePerson(@PathVariable UUID uuid,
                               @RequestBody Person person)
            throws ValidationException {
        return personService.updatePerson(uuid, person);
    }

    @DeleteMapping("/person/{uuid}")
    public void deletePerson(@PathVariable UUID uuid) {
        personService.deletePerson(uuid);
    }

    @PatchMapping("/person/{uuid}")
    public Person patchPerson(@PathVariable UUID uuid, @RequestBody Map<String, Object> updates) throws ValidationException {
        return personService.patchPerson(uuid, updates);
    }

    @PostMapping("/person/login")
    public Person login(@RequestBody LoginDTO loginDTO) throws ValidationException {
        return personService.login(loginDTO.getEmail(), loginDTO.getPassword());
    }

}
