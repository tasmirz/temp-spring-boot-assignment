package com.example.assignment1.controller;

import com.example.assignment1.repository.CourseRepository;
import com.example.assignment1.repository.Student;
import com.example.assignment1.repository.StudentRepository;
import com.example.assignment1.repository.Users;
import com.example.assignment1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all students - Teachers only
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    // Get specific student - Teachers only
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a course to a student - Teachers only
    @PostMapping("/{studentId}/enroll/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Student> enrollStudentInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return studentRepository.findById(studentId).flatMap(student -> 
            courseRepository.findById(courseId).map(course -> {
                student.addCourse(course);
                studentRepository.save(student);
                return ResponseEntity.ok(student);
            })
        ).orElse(ResponseEntity.notFound().build());
    }

    // Remove a course from a student - Teachers only
    @DeleteMapping("/{studentId}/unenroll/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Student> unenrollStudentFromCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return studentRepository.findById(studentId).flatMap(student -> 
            courseRepository.findById(courseId).map(course -> {
                student.removeCourse(course);
                studentRepository.save(student);
                return ResponseEntity.ok(student);
            })
        ).orElse(ResponseEntity.notFound().build());
    }

    // Student self-service endpoints
    
    // Get current student's profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Student> getCurrentStudent(Authentication auth) {
        String username = auth.getName();
        Student student = studentRepository.findByName(username);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    // Student enrolls themselves in a course
    @PostMapping("/me/enroll/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Student> enrollMyselfInCourse(Authentication auth, @PathVariable Long courseId) {
        String username = auth.getName();
        Student student = studentRepository.findByName(username);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return courseRepository.findById(courseId).map(course -> {
            student.addCourse(course);
            studentRepository.save(student);
            return ResponseEntity.ok(student);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Student unenrolls themselves from a course
    @DeleteMapping("/me/unenroll/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Student> unenrollMyselfFromCourse(Authentication auth, @PathVariable Long courseId) {
        String username = auth.getName();
        Student student = studentRepository.findByName(username);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return courseRepository.findById(courseId).map(course -> {
            student.removeCourse(course);
            studentRepository.save(student);
            return ResponseEntity.ok(student);
        }).orElse(ResponseEntity.notFound().build());
    }

}
