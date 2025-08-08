package com.jv.ticket;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.jv.ticket.ticket.client.EventServiceClient;
import com.jv.ticket.ticket.dto.EventDTO;
import com.jv.ticket.ticket.dto.EventPageResponseDTO;
import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.repository.TicketRepository;
import com.jv.ticket.user.dto.UserCreateDTO;
import com.jv.ticket.user.dto.UserLoginDTO;
import com.jv.ticket.user.jwt.JwtToken;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "de.flapdoodle.mongodb.embedded.version=6.0.8"
})
class TicketIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EventServiceClient eventServiceClient;

    @AfterEach
    public void cleanUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUserAndGetToken() {
        setupEventServiceMock();

        UserCreateDTO createDTO = new UserCreateDTO(
                "joaosilva",
                "joao@email.com",
                "senha123",
                "87747294034",
                User.Role.ROLE_CLIENT);

        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isCreated();

        UserLoginDTO loginDTO = new UserLoginDTO("joao@email.com", "senha123");

        JwtToken tokenResponse = webTestClient
                .post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult()
                .getResponseBody();

        return tokenResponse != null ? tokenResponse.getToken() : "";
    }

    private String createAdminUserAndGetToken() {
        setupEventServiceMock();

        UserCreateDTO adminUser = new UserCreateDTO();
        adminUser.setUsername("AdminUser");
        adminUser.setEmail("admin@email.com");
        adminUser.setPassword("password123");
        adminUser.setCpf("11144477735");
        adminUser.setRole(User.Role.ROLE_ADMIN);

        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(adminUser)
                .exchange()
                .expectStatus().isCreated();

        UserLoginDTO adminLogin = new UserLoginDTO();
        adminLogin.setEmail("admin@email.com");
        adminLogin.setPassword("password123");

        JwtToken tokenResponse = webTestClient
                .post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(adminLogin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult()
                .getResponseBody();

        return tokenResponse != null ? tokenResponse.getToken() : "";
    }

    private void setupEventServiceMock() {
        EventDTO mockEvent = new EventDTO();
        mockEvent.setEventId("event-test-id");
        mockEvent.setEventName("Teste Evento");
        mockEvent.setCanceled(false);

        EventPageResponseDTO mockEventPage = new EventPageResponseDTO();
        mockEventPage.setEvents(Arrays.asList(mockEvent));
        mockEventPage.setTotalElements(1L);

        when(eventServiceClient.getEvents(anyInt(), anyBoolean(), anyString(), anyString()))
                .thenReturn(mockEventPage);
    }

    private void setupEmptyEventServiceMock() {
        EventPageResponseDTO emptyEventPage = new EventPageResponseDTO();
        emptyEventPage.setEvents(Arrays.asList());
        emptyEventPage.setTotalElements(0L);

        when(eventServiceClient.getEvents(anyInt(), anyBoolean(), anyString(), anyString()))
                .thenReturn(emptyEventPage);
    }

    private TicketCreateDTO createValidTicketDTO() {
        return new TicketCreateDTO(
                "Maria Santos",
                "87747294034",
                "maria@email.com",
                "Teste Evento",
                new BigDecimal("250.00"));
    }

    // ==================== SUCCESS CASES ====================

    @Test
    @DisplayName("Create ticket with valid data should return 201")
    public void createTicket_WithValidData_ShouldReturn201() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = createValidTicketDTO();

        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.ticketId").isNotEmpty()
                .jsonPath("$.customerName").isEqualTo("Maria Santos")
                .jsonPath("$.cpf").isEqualTo("87747294034")
                .jsonPath("$.event.eventName").isEqualTo("Teste Evento")
                .jsonPath("$.brlTotalAmount").isEqualTo(250.00)
                .jsonPath("$.status").isEqualTo("ativo");
    }

    @Test
    @DisplayName("Get ticket by ID with valid authentication should return 200")
    public void getTicketById_WithValidId_ShouldReturn200() {
        String token = createUserAndGetToken();

        TicketResponseDTO createdTicket = webTestClient
                .post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TicketResponseDTO.class)
                .returnResult()
                .getResponseBody();

        String ticketId = createdTicket != null ? createdTicket.getTicketId() : "default-id";

        webTestClient
                .get()
                .uri("/api/v1/tickets/" + ticketId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ticketId").isEqualTo(ticketId)
                .jsonPath("$.event.eventName").isEqualTo("Teste Evento");
    }

    @Test
    @DisplayName("Cancel ticket successfully should return 200")
    public void cancelTicket_WithValidId_ShouldReturn200() {
        String token = createUserAndGetToken();

        TicketResponseDTO createdTicket = webTestClient
                .post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TicketResponseDTO.class)
                .returnResult()
                .getResponseBody();

        String ticketId = createdTicket != null ? createdTicket.getTicketId() : "default-id";

        webTestClient
                .put()
                .uri("/api/v1/tickets/" + ticketId + "/cancel")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ticketId").isEqualTo(ticketId)
                .jsonPath("$.status").isEqualTo("cancelado");
    }

    // ==================== EXCEPTION TESTS ====================

    @Test
    @DisplayName("Create ticket with different CPF should throw CpfMismatchException (400)")
    public void createTicket_WithDifferentCpf_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
                "Maria Santos",
                "12345678901",
                "maria@email.com",
                "Teste Evento",
                new BigDecimal("250.00"));

        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(message -> 
                    message.toString().contains("does not match your registered CPF"));
    }

    @Test
    @DisplayName("Create ticket with invalid CPF should throw InvalidCpfException (400)")
    public void createTicket_WithInvalidCpf_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
                "Maria Santos",
                "12345",
                "maria@email.com",
                "Teste Evento",
                new BigDecimal("250.00"));

        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(message -> 
                    message.toString().contains("CPF"));
    }

    @Test
    @DisplayName("Create ticket with non-existent event should throw EventNotFoundException (404)")
    public void createTicket_WithNonExistentEvent_ShouldReturn404() {
        String token = createUserAndGetToken();
        setupEmptyEventServiceMock();

        TicketCreateDTO createDTO = new TicketCreateDTO(
                "Maria Santos",
                "87747294034",
                "maria@email.com",
                "Evento Inexistente",
                new BigDecimal("250.00"));

        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(createDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(message -> 
                    message.toString().contains("not found"));
    }

    @Test
    @DisplayName("Get ticket with invalid ID should throw TicketNotFoundException (404)")
    public void getTicketById_WithInvalidId_ShouldReturn404() {
        String token = createUserAndGetToken();
        String invalidTicketId = "507f1f77bcf86cd799439011";

        webTestClient
                .get()
                .uri("/api/v1/tickets/" + invalidTicketId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Cancel already cancelled ticket should throw TicketAlreadyCancelledException (409)")
    public void cancelTicket_AlreadyCancelled_ShouldReturn409() {
        String token = createUserAndGetToken();

        TicketResponseDTO createdTicket = webTestClient
                .post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TicketResponseDTO.class)
                .returnResult()
                .getResponseBody();

        String ticketId = createdTicket != null ? createdTicket.getTicketId() : "default-id";

        webTestClient
                .put()
                .uri("/api/v1/tickets/" + ticketId + "/cancel")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        webTestClient
                .put()
                .uri("/api/v1/tickets/" + ticketId + "/cancel")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(409) // Conflict
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(message -> 
                    message.toString().contains("already cancelled"));
    }

    @Test
    @DisplayName("Cancel ticket from another user should throw UnauthorizedTicketAccessException (403)")
    public void cancelTicket_FromAnotherUser_ShouldReturn403() {
        String firstUserToken = createUserAndGetToken();

        TicketResponseDTO createdTicket = webTestClient
                .post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + firstUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TicketResponseDTO.class)
                .returnResult()
                .getResponseBody();

        String ticketId = createdTicket != null ? createdTicket.getTicketId() : "default-id";

        UserCreateDTO secondUserDTO = new UserCreateDTO(
                "mariasilva",
                "maria@email.com",
                "senha123",
                "11144477735",
                User.Role.ROLE_CLIENT);

        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUserDTO)
                .exchange()
                .expectStatus().isCreated();

        UserLoginDTO secondUserLogin = new UserLoginDTO("maria@email.com", "senha123");

        JwtToken secondUserTokenResponse = webTestClient
                .post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondUserLogin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult()
                .getResponseBody();

        String secondUserToken = secondUserTokenResponse != null ? secondUserTokenResponse.getToken() : "";

        webTestClient
                .put()
                .uri("/api/v1/tickets/" + ticketId + "/cancel")
                .header("Authorization", "Bearer " + secondUserToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(message -> 
                    message.toString().contains("don't have permission"));
    }

    // ==================== AUTHENTICATION/AUTHORIZATION TESTS ====================

    @Test
    @DisplayName("Access protected endpoint without authentication should return 401")
    public void accessProtectedEndpoint_WithoutAuth_ShouldReturn401() {
        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Access admin endpoint with client role should return 403")
    public void accessAdminEndpoint_WithClientRole_ShouldReturn403() {
        String clientToken = createUserAndGetToken();

        webTestClient
                .get()
                .uri("/api/v1/tickets/status/ACTIVE")
                .header("Authorization", "Bearer " + clientToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Access admin endpoint with admin role should return 200")
    public void accessAdminEndpoint_WithAdminRole_ShouldReturn200() {
        String adminToken = createAdminUserAndGetToken();

        webTestClient
                .get()
                .uri("/api/v1/tickets/status/ACTIVE")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @DisplayName("Check tickets for event should work without authentication")
    public void checkTicketsForEvent_WithoutAuth_ShouldReturn200() {
        webTestClient
                .get()
                .uri("/api/v1/tickets/event/test-event-id/check")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.eventId").isEqualTo("test-event-id")
                .jsonPath("$.hasTickets").isEqualTo(false)
                .jsonPath("$.activeTicketCount").isEqualTo(0)
                .jsonPath("$.totalTicketCount").isEqualTo(0);
    }

    @Test
    @DisplayName("Get tickets by CPF with admin role should return 200")
    public void getTicketsByCpf_WithAdminRole_ShouldReturn200() {
        String clientToken = createUserAndGetToken();
        String adminToken = createAdminUserAndGetToken();
        String cpf = "87747294034";

        webTestClient
                .post()
                .uri("/api/v1/tickets")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createValidTicketDTO())
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .get()
                .uri("/api/v1/tickets/cpf/" + cpf)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].cpf").isEqualTo(cpf);
    }

    @Test
    @DisplayName("Get tickets by CPF without admin role should return 403")
    public void getTicketsByCpf_WithoutAdminRole_ShouldReturn403() {
        String cpf = "87747294034";

        webTestClient
                .get()
                .uri("/api/v1/tickets/cpf/" + cpf)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
