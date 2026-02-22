package com.example.assignment1.controller;

import com.example.assignment1.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourseControllerTest {
    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void clean() {
        courseRepository.deleteAll();
    }

    @Test
    @Disabled
    void createAndFetchCourse() throws Exception {
        String json = "{\"name\":\"Physics\",\"code\":\"PHY101\"}";

        String base = "http://localhost:" + port;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> postResp = restTemplate.postForEntity(base + "/api/courses", entity, String.class);
        assert(postResp.getStatusCode().is2xxSuccessful());
        assert(postResp.getBody().contains("Physics"));

        ResponseEntity<String> getResp = restTemplate.getForEntity(base + "/api/courses", String.class);
        assert(getResp.getStatusCode().is2xxSuccessful());
        assert(getResp.getBody().contains("PHY101"));
    }
}
