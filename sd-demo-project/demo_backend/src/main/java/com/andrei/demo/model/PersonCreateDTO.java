package com.andrei.demo.model;

import com.andrei.demo.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PersonCreateDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message =
            "Name should be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Password is required")
    @StrongPassword(message =
            "Password must contain at least 8 characters, including uppercase, " +
                    "lowercase, digit, and special character")
    private String password;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Email is required")
    private String email;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}