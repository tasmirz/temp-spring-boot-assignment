package com.example.assignment1.controller;

import com.example.assignment1.repository.Teacher;
import com.example.assignment1.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher create(@RequestBody Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Teacher> getById(@PathVariable Long id) {
        Optional<Teacher> t = teacherRepository.findById(id);
        return t.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Teacher> update(@PathVariable Long id, @RequestBody Teacher body) {
        return teacherRepository.findById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setEmail(body.getEmail());
            existing.setSubject(body.getSubject());
            teacherRepository.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
