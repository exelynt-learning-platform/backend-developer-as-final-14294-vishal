package com.resourcebooking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginWithValidCredentials_shouldReturnJwt() throws Exception {

        String body = """
                {
                    "username": "user",
                    "password": "User@123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/login"),
                        request,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode json =
                objectMapper.readTree(response.getBody());

        assertTrue(json.get("success").asBoolean());

        assertNotNull(
                json.at("/data/token").asText(null)
        );
    }

    @Test
    void loginWithInvalidPassword_shouldReturnUnauthorized() {

        String body = """
                {
                    "username": "user",
                    "password": "WrongPassword"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/login"),
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );
    }

    private String url(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }
}