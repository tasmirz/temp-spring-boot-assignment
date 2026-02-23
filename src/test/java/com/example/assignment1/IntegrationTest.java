package com.example.assignment1;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {

    @Test
    void indexTemplatePresent() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/index.html");
        assertNotNull(is, "templates/index.html should be present on classpath");

        String content;
        try (Scanner s = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A")) {
            content = s.hasNext() ? s.next() : "";
        }

        assertTrue(content.contains("<html") || content.contains("<!DOCTYPE"), "index.html should contain HTML");
    }
}
