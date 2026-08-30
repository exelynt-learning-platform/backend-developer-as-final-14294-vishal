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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ReservationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userToken = login("user", "User@123");
        adminToken = login("admin", "Admin@123");
    }

    @Test
    void userShouldBeAbleToCreateReservation() {

        String body = """
                {
                    "resourceId": 2,
                    "startTime": "2026-09-10T10:00:00",
                    "endTime": "2026-09-10T12:00:00"
                }
                """;

        HttpHeaders headers = bearer(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/reservations"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );
    }

    @Test
    void reservationRequestShouldNotRequireUserId() {

        String body = """
                {
                    "resourceId": 1,
                    "startTime": "2026-09-11T12:00:00",
                    "endTime": "2026-09-11T14:00:00"
                }
                """;

        HttpHeaders headers = bearer(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/reservations"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertNotEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void invalidTimeRangeShouldBeRejected() {

        String body = """
                {
                    "resourceId": 1,
                    "startTime": "2026-09-12T14:00:00",
                    "endTime": "2026-09-12T10:00:00"
                }
                """;

        HttpHeaders headers = bearer(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/reservations"),
                        new HttpEntity<>(body, headers),
                        String.class
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void userShouldBeAbleToViewReservations() {

        HttpHeaders headers = bearer(userToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/reservations"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );
    }

    @Test
    void adminShouldBeAbleToViewAllReservations() {

        HttpHeaders headers = bearer(adminToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/reservations"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
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