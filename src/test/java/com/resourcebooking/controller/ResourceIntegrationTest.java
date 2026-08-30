package com.resourcebooking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.resttestclient.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ResourceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin", "Admin@123");
        userToken = login("user", "User@123");
    }

    @Test
    void userShouldBeAbleToReadResources() {

        HttpHeaders headers = bearer(userToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/resources"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void adminShouldBeAbleToCreateResource() {

        String body = """
                {
                    "name": "Test Meeting Room A1",
                    "description": "Automated test resource",
                    "location": "Pune",
                    "capacity": 10,
                    "price": 500.00,
                    "available": true
                }
                """;

        HttpHeaders headers = bearer(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/resources"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );
    }

    @Test
    void userShouldNotBeAbleToCreateResource() {

        String body = """
                {
                   "name": "User Test Room",
                                               "description": "Test resource",
                                               "location": "Building A",
                                               "capacity": 10,
                                               "price": 100.00,
                                               "available": true
                }
                """;

        HttpHeaders headers = bearer(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/resources"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }

    @Test
    void userShouldNotBeAbleToDeleteResource() {

        HttpHeaders headers = bearer(userToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/resources/1"),
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }

    private String login(String username, String password)
            throws Exception {

        String body = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/login"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode json =
                objectMapper.readTree(response.getBody());

        return json.at("/data/token").asText();
    }

    private HttpHeaders bearer(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return headers;
    }

    private String url(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }
}