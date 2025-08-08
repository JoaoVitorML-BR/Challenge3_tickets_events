package com.jv.ticket;

import com.jv.ticket.user.dto.UserCreateDTO;
import com.jv.ticket.user.dto.UserResponseDTO;
import com.jv.ticket.user.dto.UserUpdateDTO;
import com.jv.ticket.user.dto.UserLoginDTO;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.repository.UserRepository;
import com.jv.ticket.user.jwt.JwtToken;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "de.flapdoodle.mongodb.embedded.version=6.0.8"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserIT {

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

    private String createUserAndGetToken(String username, String email, String password, String cpf, User.Role role) {
        UserCreateDTO createDTO = new UserCreateDTO(username, email, password, cpf, role);
        
        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isCreated();

        UserLoginDTO loginDTO = new UserLoginDTO(email, password);

        JwtToken response = webTestClient.post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult().getResponseBody();

        return response != null ? response.getToken() : null;
    }

    // === SUCCESS CASES ===

    @Test
    @DisplayName("Should create user successfully")
    void createUser_ValidData_ShouldReturnCreated() {
        UserCreateDTO dto = new UserCreateDTO("testuser", "test@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);

        UserResponseDTO response = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getCpf()).isEqualTo("11144477735");
        assertThat(response.getRole()).isEqualTo(User.Role.ROLE_CLIENT);
    }

    @Test
    @DisplayName("Should get user by ID when authenticated as owner")
    void getUserById_AsOwner_ShouldReturnUserData() {
        String token = createUserAndGetToken("testuser", "test@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        
        User createdUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertThat(createdUser).isNotNull();
        
        UserResponseDTO response = webTestClient.get()
                .uri("/api/v1/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should update user when authenticated as owner")
    void updateUser_AsOwner_ShouldReturnUpdatedUser() {
        String token = createUserAndGetToken("originaluser", "original@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        User user = userRepository.findByEmail("original@example.com").orElse(null);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("updateduser", "updated@example.com", "87747294034", "password123");

        UserResponseDTO response = webTestClient.put()
                .uri("/api/v1/users/" + user.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        if (response != null) {
            assertThat(response.getUsername()).isEqualTo("updateduser");
            assertThat(response.getEmail()).isEqualTo("updated@example.com");
        }
    }

    // === EXCEPTION TESTS ===

    @Test
    @DisplayName("Should return 409 CONFLICT for duplicate username - UsernameUniqueViolationException")
    void createUser_DuplicateUsername_ShouldReturn409() {
        UserCreateDTO firstUser = new UserCreateDTO("duplicateuser", "first@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstUser)
                .exchange()
                .expectStatus().isCreated();

        UserCreateDTO secondUser = new UserCreateDTO("duplicateuser", "second@example.com", "password456", "87747294034", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUser)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("Should return 409 CONFLICT for duplicate email - EmailUniqueViolationException")
    void createUser_DuplicateEmail_ShouldReturn409() {
        UserCreateDTO firstUser = new UserCreateDTO("firstuser", "duplicate@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstUser)
                .exchange()
                .expectStatus().isCreated();

        UserCreateDTO secondUser = new UserCreateDTO("seconduser", "duplicate@example.com", "password456", "87747294034", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUser)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST for invalid CPF - CpfViolationException")
    void createUser_InvalidCpf_ShouldReturn400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "password123", "11111111111", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST for empty data - EmptyDataException")
    void createUser_EmptyData_ShouldReturn400() {
        UserCreateDTO dto = new UserCreateDTO("", "", "", "", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 404 NOT_FOUND for non-existent user - UserNotFoundException")
    void getUserById_NonExistentUser_ShouldReturn404() {
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "11144477735", User.Role.ROLE_ADMIN);

        webTestClient.get()
                .uri("/api/v1/users/507f1f77bcf86cd799439011")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return 404 NOT_FOUND when updating non-existent user - UserNotFoundException")
    void updateUser_NonExistentUser_ShouldReturn404() {
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "11144477735", User.Role.ROLE_ADMIN);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("newname", "newemail@example.com", "87747294034", "password123");

        webTestClient.put()
                .uri("/api/v1/users/507f1f77bcf86cd799439011")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    // === AUTHENTICATION & AUTHORIZATION TESTS ===

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when accessing user without token")
    void getUserById_NoToken_ShouldReturn401() {
        createUserAndGetToken("testuser", "test@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        User user = userRepository.findByEmail("test@example.com").orElse(null);

        webTestClient.get()
                .uri("/api/v1/users/" + user.getId())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should return 403 FORBIDDEN when regular user tries to access other user data")
    void getUserById_AccessOtherUser_ShouldReturn403() {
        createUserAndGetToken("user1", "user1@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        String token2 = createUserAndGetToken("user2", "user2@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        
        User user1 = userRepository.findByEmail("user1@example.com").orElse(null);

        webTestClient.get()
                .uri("/api/v1/users/" + user1.getId())
                .header("Authorization", "Bearer " + token2)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should allow admin to access other user data")
    void getUserById_AdminAccessingOtherUser_ShouldReturn200() {
        createUserAndGetToken("regularuser", "regular@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        User regularUser = userRepository.findByEmail("regular@example.com").orElse(null);
        
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "87747294034", User.Role.ROLE_ADMIN);

        UserResponseDTO response = webTestClient.get()
                .uri("/api/v1/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        if (response != null) {
            assertThat(response.getUsername()).isEqualTo("regularuser");
        }
    }

    @Test
    @DisplayName("Should return 403 FORBIDDEN when regular user tries to get user by CPF")
    void getUserByCpf_RegularUser_ShouldReturn403() {
        createUserAndGetToken("targetuser", "target@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        String clientToken = createUserAndGetToken("clientuser", "client@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);

        webTestClient.get()
                .uri("/api/v1/users/cpf/11144477735")
                .header("Authorization", "Bearer " + clientToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should allow admin to get user by CPF")
    void getUserByCpf_AdminUser_ShouldReturn200() {
        createUserAndGetToken("targetuser", "target@example.com", "password123", "11144477735", User.Role.ROLE_CLIENT);
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "87747294034", User.Role.ROLE_ADMIN);

        UserResponseDTO response = webTestClient.get()
                .uri("/api/v1/users/cpf/11144477735")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        if (response != null) {
            assertThat(response.getCpf()).isEqualTo("11144477735");
            assertThat(response.getUsername()).isEqualTo("targetuser");
        }
    }
}
