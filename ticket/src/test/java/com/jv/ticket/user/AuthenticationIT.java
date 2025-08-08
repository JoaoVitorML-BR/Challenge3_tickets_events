package com.jv.ticket.user;

import com.jv.ticket.user.dto.UserCreateDTO;
import com.jv.ticket.user.dto.UserLoginDTO;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
                "de.flapdoodle.mongodb.embedded.version=6.0.8"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationIT {

        @LocalServerPort
        private int port;

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setup() throws Exception {
                userRepository.deleteAll();
        }

        @AfterEach
        void cleanup() {
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("Should return bad request for authentication with invalid email format")
        void testAuthentication_InvalidEmailFormat_ReturnsStatus400() {
                UserLoginDTO loginDTO = new UserLoginDTO("invalid.email", "password123");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return bad request for authentication with empty email")
        void testAuthentication_EmptyEmail_ReturnsStatus400() {
                UserLoginDTO loginDTO = new UserLoginDTO("", "password123");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return bad request for authentication with empty password")
        void testAuthentication_EmptyPassword_ReturnsStatus400() {
                UserLoginDTO loginDTO = new UserLoginDTO("valid@example.com", "");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return bad request for authentication with short password")
        void testAuthentication_ShortPassword_ReturnsStatus400() {
                UserLoginDTO loginDTO = new UserLoginDTO("valid@example.com", "123");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return bad request for authentication with wrong credentials")
        void testAuthentication_WrongCredentials_ReturnsStatus400() {
                UserCreateDTO createDTO = new UserCreateDTO("testuser", "test@example.com", "password123",
                                "64058611049",
                                User.Role.ROLE_CLIENT);

                webTestClient.post()
                                .uri("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(createDTO)
                                .exchange()
                                .expectStatus().isCreated();

                UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "wrongpassword");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return bad request for authentication with non-existent user")
        void testAuthentication_NonExistentUser_ReturnsStatus400() {
                UserLoginDTO loginDTO = new UserLoginDTO("nonexistent@example.com", "password123");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should authenticate user successfully")
        void testAuthentication_ValidCredentials_ReturnsToken() {
                UserCreateDTO createDTO = new UserCreateDTO("authuser", "auth@example.com", "password123",
                                "87747294034",
                                User.Role.ROLE_CLIENT);

                webTestClient.post()
                                .uri("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(createDTO)
                                .exchange()
                                .expectStatus().isCreated();

                UserLoginDTO loginDTO = new UserLoginDTO("auth@example.com", "password123");

                webTestClient.post()
                                .uri("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginDTO)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.token").exists()
                                .jsonPath("$.token").isNotEmpty();
        }
}
