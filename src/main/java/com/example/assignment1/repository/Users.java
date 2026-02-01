package com.example.assignment1.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class Users {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    // had to change from AUTO to IDENTITY for postgres, idk why
    private Long id;
    private String name;
    private String role;
    private String password;

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + '\'' + ", role='" + role + '\'' + ", password='" + password + '\'' + '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String rawPassword) {
        // log encoded and raw passwords for debugging
        System.out.println("Checking password: raw=" + rawPassword + ", encoded=" + this.password);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.password);
    }

    public void hashPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(this.password);
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
