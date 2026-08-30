package com.resourcebooking.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void protectedReservationEndpointWithoutToken_shouldReturnUnauthorized() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        url("/api/reservations"),
                        String.class
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );
    }

    @Test
    void protectedResourceCreationWithoutToken_shouldReturnUnauthorized() {

        String body = """
                {
                    "name": "Meeting Room",
                                               "description": "A meeting room",
                                               "location": "Building A",
                                               "capacity": 10,
                                               "price": 100.00,
                                               "available": true
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/resources"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );
    }

    @Test
    void loginEndpointShouldBePublic() {

        String body = """
                {
                    "username": "invalid",
                    "password": "invalid"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/login"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        // Endpoint itself is accessible.
        // Authentication failure should therefore be 401,
        // rather than Spring Security blocking the endpoint.
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );
    }

    private String url(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }
}