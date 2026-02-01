package com.example.assignment1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assignment1.repository.Users;
import com.example.assignment1.service.JwtService;
import com.example.assignment1.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/")
    public String home() {
        return userRepository.findAll().toString();
    }

    @PostMapping("/")
    // gets {name, password} from request body
    public Map<String, String> authenticate(@RequestBody Users user) {
        Users existingUser = userRepository.findByName(user.getName());
        System.out.println("Authenticating user: " + user.getName());
        System.out.println("Existing user: " + existingUser);
        if (existingUser != null && existingUser.checkPassword(user.getPassword())) {
            String token = jwtService.generateToken(existingUser.getName(), existingUser.getRole());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return response;
        } else {
            // return 403
            throw new RuntimeException("Invalid credentials");
        }
    }

    @PostMapping("/register")
    // expect {name, password, role} in request body
    public String registerUser(@RequestBody Users user) {
        // Registration logic goes here
        user.hashPassword();
        return userRepository.save(user).toString(); 
    }
}
