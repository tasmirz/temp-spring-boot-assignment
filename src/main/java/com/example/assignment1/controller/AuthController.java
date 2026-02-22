package com.example.assignment1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assignment1.repository.Users;
import com.example.assignment1.repository.UserRepository;
import com.example.assignment1.repository.Teacher;
import com.example.assignment1.repository.TeacherRepository;
import com.example.assignment1.repository.Student;
import com.example.assignment1.repository.StudentRepository;
import com.example.assignment1.service.JwtService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private StudentRepository studentRepository;

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
        System.out.println("Registering user: " + user.getName() + " with role: " + user.getRole());
        // Hash password and save user
        user.hashPassword();
        Users savedUser = userRepository.save(user);
        System.out.println("User saved: " + savedUser);
        
        String role = savedUser.getRole();
        // Normalize role - handle both "TEACHER" and "ROLE_TEACHER"
        boolean isTeacher = "TEACHER".equals(role) || "ROLE_TEACHER".equals(role);
        boolean isStudent = "STUDENT".equals(role) || "ROLE_STUDENT".equals(role);
        
        System.out.println("Role: " + role + ", isTeacher: " + isTeacher + ", isStudent: " + isStudent);
        
        // Create Teacher or Student record based on role
        // Don't set ID - let the database auto-generate it
        if (isTeacher) {
            Teacher teacher = new Teacher();
            teacher.setName(savedUser.getName());
            teacher.setEmail(savedUser.getName() + "@university.edu");
            Teacher savedTeacher = teacherRepository.save(teacher);
            System.out.println("Teacher saved: " + savedTeacher);
        } else if (isStudent) {
            Student student = new Student();
            student.setName(savedUser.getName());
            student.setEmail(savedUser.getName() + "@university.edu");
            Student savedStudent = studentRepository.save(student);
            System.out.println("Student saved: " + savedStudent);
        }
        
        return savedUser.toString();
    }
}
