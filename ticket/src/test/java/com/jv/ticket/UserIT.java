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

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser_ReturnsCreatedUser_Status201() {
        UserCreateDTO dto = new UserCreateDTO("newuser", "newuser@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);

        UserResponseDTO response = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getCpf()).isEqualTo("87747294034");
        assertThat(response.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should return bad request for invalid user data")
    void testCreateUser_InvalidInput_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("  ", "invalid-email", "12345", "12345678901", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for invalid CPF")
    void testCreateUser_InvalidCPF_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "password123", "11111111111", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for short password")
    void testCreateUser_ShortPassword_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return conflict for duplicate username")
    void testCreateUserWithDuplicateUsername_ReturnsConflict() {
        UserCreateDTO firstUser = new UserCreateDTO("duplicateuser", "first@example.com", "password123", "64058611049", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstUser)
                .exchange()
                .expectStatus().isCreated();

        UserCreateDTO secondUser = new UserCreateDTO("duplicateuser", "second@example.com", "password456", "62569691038", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUser)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("Should return conflict for duplicate email")
    void testCreateUserWithDuplicateEmail_ReturnsConflict() {
        UserCreateDTO firstUser = new UserCreateDTO("firstuser", "duplicate@example.com", "password123", "64058611049", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstUser)
                .exchange()
                .expectStatus().isCreated();

        UserCreateDTO secondUser = new UserCreateDTO("seconduser", "duplicate@example.com", "password456", "62569691038", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUser)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("Should return conflict for duplicate CPF")
    void testCreateUserWithDuplicateCPF_ReturnsConflict() {
        UserCreateDTO firstUser = new UserCreateDTO("firstuser", "first@example.com", "password123", "79972852024", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstUser)
                .exchange()
                .expectStatus().isCreated();

        UserCreateDTO secondUser = new UserCreateDTO("seconduser", "second@example.com", "password456", "79972852024", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUser)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("Should return bad request for empty username")
    void testCreateUser_EmptyUsername_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("", "valid@example.com", "password123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for empty email")
    void testCreateUser_EmptyEmail_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "", "password123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for empty password")
    void testCreateUser_EmptyPassword_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for empty CPF")
    void testCreateUser_EmptyCPF_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "password123", "", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for username too short")
    void testCreateUser_UsernameTooShort_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("abc", "valid@example.com", "password123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for username too long")
    void testCreateUser_UsernameTooLong_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("averylongusernamethatexceedsthemaximumlimit", "valid@example.com", "password123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for password too long")
    void testCreateUser_PasswordTooLong_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "averylongpasswordthatexceedsthemaximumlimit", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return bad request for malformed email")
    void testCreateUser_MalformedEmail_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "invalid.email.com", "password123", "08711369027", User.Role.ROLE_CLIENT);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should create user with default CLIENT role when role is null")
    void testCreateUser_NullRole_CreatesWithDefaultRole() {
        UserCreateDTO dto = new UserCreateDTO("validuser", "valid@example.com", "password123", "08711369027", null);

        UserResponseDTO response = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(User.Role.ROLE_CLIENT);
    }

    @Test
    @DisplayName("Should create user with ADMIN role successfully")
    void testCreateUser_AdminRole_ReturnsCreatedUser_Status201() {
        UserCreateDTO dto = new UserCreateDTO("adminuser", "admin@example.com", "password123", "62569691038", User.Role.ROLE_ADMIN);

        UserResponseDTO response = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("adminuser");
        assertThat(response.getRole()).isEqualTo(User.Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should handle multiple validation errors")
    void testCreateUser_MultipleValidationErrors_ReturnsStatus400() {
        UserCreateDTO dto = new UserCreateDTO(null, null, null, null, null);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();
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

    @Test
    @DisplayName("Should get user by ID when user is owner")
    void testGetUserById_UserIsOwner_ReturnsUserData() {
        String token = createUserAndGetToken("testuser", "test@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        
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
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should get user by ID when user is admin")
    void testGetUserById_UserIsAdmin_ReturnsUserData() {
        createUserAndGetToken("regularuser", "regular@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        User regularUser = userRepository.findByEmail("regular@example.com").orElse(null);
        
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "62569691038", User.Role.ROLE_ADMIN);

        UserResponseDTO response = webTestClient.get()
                .uri("/api/v1/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("regularuser");
    }

    @Test
    @DisplayName("Should return forbidden when user tries to access other user data")
    void testGetUserById_UserAccessingOtherUser_ReturnsForbidden() {
        createUserAndGetToken("user1", "user1@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        String token2 = createUserAndGetToken("user2", "user2@example.com", "password123", "62569691038", User.Role.ROLE_CLIENT);
        
        User user1 = userRepository.findByEmail("user1@example.com").orElse(null);

        webTestClient.get()
                .uri("/api/v1/users/" + user1.getId())
                .header("Authorization", "Bearer " + token2)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should return unauthorized when accessing user by ID without token")
    void testGetUserById_NoToken_ReturnsUnauthorized() {
        createUserAndGetToken("testuser", "test@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        User user = userRepository.findByEmail("test@example.com").orElse(null);

        webTestClient.get()
                .uri("/api/v1/users/" + user.getId())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should get user by CPF when user is admin")
    void testGetUserByCpf_AdminUser_ReturnsUserData() {
        createUserAndGetToken("targetuser", "target@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "62569691038", User.Role.ROLE_ADMIN);

        UserResponseDTO response = webTestClient.get()
                .uri("/api/v1/users/cpf/87747294034")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getCpf()).isEqualTo("87747294034");
        assertThat(response.getUsername()).isEqualTo("targetuser");
    }

    @Test
    @DisplayName("Should return forbidden when regular user tries to get user by CPF")
    void testGetUserByCpf_RegularUser_ReturnsForbidden() {
        createUserAndGetToken("targetuser", "target@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        String clientToken = createUserAndGetToken("clientuser", "client@example.com", "password123", "62569691038", User.Role.ROLE_CLIENT);

        webTestClient.get()
                .uri("/api/v1/users/cpf/87747294034")
                .header("Authorization", "Bearer " + clientToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should return not found when getting user by non-existent CPF")
    void testGetUserByCpf_NonExistentCpf_ReturnsNotFound() {
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "62569691038", User.Role.ROLE_ADMIN);

        webTestClient.get()
                .uri("/api/v1/users/cpf/12345678900")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should get all users with pagination when user is admin")
    void testGetAllUsers_AdminUser_ReturnsPaginatedUsers() {
        createUserAndGetToken("user1", "user1@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        createUserAndGetToken("user2", "user2@example.com", "password123", "62569691038", User.Role.ROLE_CLIENT);
        createUserAndGetToken("user3", "user3@example.com", "password123", "08711369027", User.Role.ROLE_CLIENT);
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "64058611049", User.Role.ROLE_ADMIN);

        webTestClient.get()
                .uri("/api/v1/users?page=0&size=2")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(4)
                .jsonPath("$.totalPages").isEqualTo(2);
    }

    @Test
    @DisplayName("Should return forbidden when regular user tries to get all users")
    void testGetAllUsers_RegularUser_ReturnsForbidden() {
        String clientToken = createUserAndGetToken("clientuser", "client@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);

        webTestClient.get()
                .uri("/api/v1/users")
                .header("Authorization", "Bearer " + clientToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should update user when user is owner")
    void testUpdateUser_UserIsOwner_ReturnsUpdatedUser() {
        String token = createUserAndGetToken("originaluser", "original@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        User user = userRepository.findByEmail("original@example.com").orElse(null);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("updateduser", "updated@example.com", "62569691038", "password123");

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
        assertThat(response.getUsername()).isEqualTo("updateduser");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getCpf()).isEqualTo("62569691038");
    }

    @Test
    @DisplayName("Should update user when user is admin")
    void testUpdateUser_AdminUser_ReturnsUpdatedUser() {
        createUserAndGetToken("targetuser", "target@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        User targetUser = userRepository.findByEmail("target@example.com").orElse(null);
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "62569691038", User.Role.ROLE_ADMIN);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("adminupdated", "adminupdated@example.com", "08711369027", "password123");

        UserResponseDTO response = webTestClient.put()
                .uri("/api/v1/users/" + targetUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("adminupdated");
    }

    @Test
    @DisplayName("Should return forbidden when user tries to update other user")
    void testUpdateUser_UserUpdatingOtherUser_ReturnsForbidden() {
        createUserAndGetToken("user1", "user1@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        String token2 = createUserAndGetToken("user2", "user2@example.com", "password123", "62569691038", User.Role.ROLE_CLIENT);
        
        User user1 = userRepository.findByEmail("user1@example.com").orElse(null);
        UserUpdateDTO updateDTO = new UserUpdateDTO("hacker", "hacker@example.com", "08711369027", "password123");

        webTestClient.put()
                .uri("/api/v1/users/" + user1.getId())
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should return bad request when updating with invalid data")
    void testUpdateUser_InvalidData_ReturnsBadRequest() {
        String token = createUserAndGetToken("testuser", "test@example.com", "password123", "87747294034", User.Role.ROLE_CLIENT);
        User user = userRepository.findByEmail("test@example.com").orElse(null);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("ab", "invalid-email", "11111111111", "password123");

        webTestClient.put()
                .uri("/api/v1/users/" + user.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return not found when updating non-existent user")
    void testUpdateUser_NonExistentUser_ReturnsNotFound() {
        String adminToken = createUserAndGetToken("adminuser", "admin@example.com", "password123", "87747294034", User.Role.ROLE_ADMIN);
        
        UserUpdateDTO updateDTO = new UserUpdateDTO("newname", "newemail@example.com", "62569691038", "password123");

        webTestClient.put()
                .uri("/api/v1/users/507f1f77bcf86cd799439011")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isNotFound();
    }
}
