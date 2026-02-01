
package com.example.assignment1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.assignment1.service.JwtService;

@Controller
public class ControlledController {
    @Autowired
    private JwtService jwtService;

    @GetMapping("/controlled-teacher")
    public String controlledTeacher(Authentication authentication, Model model) {
        if (authentication != null) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");
            
            model.addAttribute("user", new UserDTO(username, role));
        }
        return "controlled-teacher";
    }

    @GetMapping("/controlled-student")
    public String controlledStudent(Authentication authentication, Model model) {
        if (authentication != null) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");
            
            model.addAttribute("user", new UserDTO(username, role));
        }
        return "controlled-student";
    }

    public static class UserDTO {
        public String name;
        public String role;

        public UserDTO(String name, String role) {
            this.name = name;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }
    }
}
