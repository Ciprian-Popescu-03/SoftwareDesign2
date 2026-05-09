package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginDTO;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.model.Person;
import com.andrei.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Random;

@RestController
@CrossOrigin
public class PersonController {

    private final PersonService personService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Inject all the required security beans alongside PersonService
    public PersonController(PersonService personService,
                            AuthenticationManager authenticationManager,
                            JwtUtil jwtUtil,
                            UserDetailsService userDetailsService) {
        this.personService = personService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
    public Person addPerson(@Valid @RequestBody PersonCreateDTO personDTO) {
        return personService.addPerson(personDTO);
    }

    @PutMapping("/person/{uuid}")
    public Person updatePerson(@PathVariable UUID uuid,
                               @RequestBody Person person) throws ValidationException {
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

    // --- Assignment 3: JWT Login Endpoint ---
    @PostMapping("/person/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 1. Authenticate using Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            // 2. Load the user
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
            Person person = (Person) userDetails; // Cast back to Person to get the role easily

            // 3. Generate the JWT Token
            String jwtToken = jwtUtil.generateToken(userDetails, person.getRole().name());

            // 4. Return token and person info to the frontend
            return ResponseEntity.ok(Map.of(
                    "token", jwtToken,
                    "person", person
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password."));
        }
    }

    // --- Assignment 3: Forgot Password Endpoints ---
    @PostMapping("/forgot-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) throws ValidationException {
        // Generate a random 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));

        // Save the code temporarily
        personService.saveResetCodeForUser(email, code);

        // Mock sending the code (e.g., via SMS or Email). Check your console to see it!
        System.out.println("==================================================");
        System.out.println("RESET CODE FOR " + email + " is: " + code);
        System.out.println("==================================================");

        return ResponseEntity.ok(Map.of("message", "Reset code sent successfully."));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String code,
                                           @RequestParam String newPassword) throws ValidationException {

        // Verify the code
        if (!personService.verifyResetCode(email, code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired code."));
        }

        // Update the password (service will hash it)
        personService.updatePassword(email, newPassword);

        // Extra credit (1.0p): Send confirmation notification
        System.out.println("Notification: Your password has been successfully changed.");

        return ResponseEntity.ok(Map.of("message", "Password successfully reset."));
    }
}