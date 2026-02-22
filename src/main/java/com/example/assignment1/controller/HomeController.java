package com.example.assignment1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}

