package com.example.assignment1.controller;

import com.example.assignment1.dto.CourseRequest;
import com.example.assignment1.repository.Course;
import com.example.assignment1.repository.CourseRepository;
import com.example.assignment1.repository.Teacher;
import com.example.assignment1.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Course> create(@RequestBody CourseRequest courseRequest, Authentication auth) {
        // Get the current authenticated user (teacher)
        String username = auth.getName();
        System.out.println("Creating course for username: " + username);
        Optional<Teacher> teacher = teacherRepository.findByName(username);
        System.out.println("Teacher lookup result: " + teacher);
        
        if (teacher.isEmpty()) {
            System.out.println("Teacher not found for username: " + username);
            return ResponseEntity.badRequest().build();
        }
        
        Course course = new Course(courseRequest.getName(), courseRequest.getCode(), teacher.get());
        Course saved = courseRepository.save(course);
        System.out.println("Course saved: " + saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        Optional<Course> c = courseRepository.findById(id);
        return c.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course body) {
        return courseRepository.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setCode(body.getCode());
            if (body.getTeacher() != null && body.getTeacher().getId() != null) {
                Optional<Teacher> teacher = teacherRepository.findById(body.getTeacher().getId());
                if (teacher.isPresent()) {
                    existing.setTeacher(teacher.get());
                }
            }
            courseRepository.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
