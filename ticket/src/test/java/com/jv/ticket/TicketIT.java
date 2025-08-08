package com.jv.ticket;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
        EventDTO mockEventDefault = new EventDTO();
        mockEventDefault.setEventId("event-test-id");
        mockEventDefault.setEventName("Teste Evento");
        mockEventDefault.setCanceled(false);
        
        EventDTO mockEventLong = new EventDTO();
        mockEventLong.setEventId("event-long-id");
        mockEventLong.setEventName("Festival Internacional de Música e Arte Contemporânea do Brasil 2024");
        mockEventLong.setCanceled(false);
        
        EventDTO mockEventAccents = new EventDTO();
        mockEventAccents.setEventId("event-accents-id");
        mockEventAccents.setEventName("Festival de Música Sertaneja - Edição Especial");
        mockEventAccents.setCanceled(false);
        
        EventPageResponseDTO mockEventPage = new EventPageResponseDTO();
        mockEventPage.setEvents(Arrays.asList(mockEventDefault, mockEventLong, mockEventAccents));
        mockEventPage.setTotalElements(3L);
        
        when(eventServiceClient.getEvents(anyInt(), anyBoolean(), anyString(), anyString()))
            .thenReturn(mockEventPage);
        
        UserCreateDTO createDTO = new UserCreateDTO(
            "joaosilva",
            "joao@email.com",
            "senha123",
            "87747294034",
            User.Role.ROLE_CLIENT
        );
        
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
    
    private TicketCreateDTO createValidTicketDTO() {
        return new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
    }
        
    @Test
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
            .jsonPath("$.customerEmail").isEqualTo("maria@email.com")
            .jsonPath("$.event.eventName").isEqualTo("Teste Evento")
            .jsonPath("$.event.eventId").isEqualTo("event-test-id")
            .jsonPath("$.brlTotalAmount").isEqualTo(250.00)
            .jsonPath("$.status").isEqualTo("ativo");
    }
    
    @Test
    public void createTicket_WithoutAuthentication_ShouldReturn401() {
        TicketCreateDTO createDTO = createValidTicketDTO();
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isUnauthorized();
    }
    
    @Test
    public void createTicket_WithInvalidToken_ShouldReturn401() {
        TicketCreateDTO createDTO = createValidTicketDTO();
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth("invalid-token"))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isUnauthorized();
    }
        
    @Test
    public void createTicket_WithEmptyCustomerName_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNullCustomerName_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            null,
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithEmptyCpf_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNullCpf_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            null,
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithInvalidEmail_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "email-invalido",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithEmptyEmail_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNullEmail_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            null,
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithEmptyEventName_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "",
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNullEventName_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            null,
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNegativeAmount_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("-10.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithZeroAmount_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("0.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
    
    @Test
    public void createTicket_WithNullAmount_ShouldReturn400() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            null
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isBadRequest();
    }
        
    @Test
    public void createTicket_WithMinimumValidAmount_ShouldReturn201() {
        String token = createUserAndGetToken();
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("0.01")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.brlTotalAmount").isEqualTo(0.01);
    }
    
    @Test
    public void createTicket_WithAccentsInEventName_ShouldReturn201() {
        String token = createUserAndGetToken();
        String eventWithAccents = "Festival de Música Sertaneja - Edição Especial";
        TicketCreateDTO createDTO = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            eventWithAccents,
            new BigDecimal("250.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.event.eventName").isEqualTo(eventWithAccents);
    }
    
    // ==================== CHECK TICKETS FOR EVENT TESTS ====================
    
    @Test
    public void checkTicketsForEvent_WithActiveTickets_ShouldReturn200() {
        String token = createUserAndGetToken();
        
        TicketCreateDTO createDTO1 = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        TicketCreateDTO createDTO2 = new TicketCreateDTO(
            "João Silva",
            "87747294034",
            "joao@email.com",
            "Teste Evento",
            new BigDecimal("300.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO1)
            .exchange()
            .expectStatus().isCreated();
            
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO2)
            .exchange()
            .expectStatus().isCreated();
        
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/event-test-id/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("event-test-id")
            .jsonPath("$.hasTickets").isEqualTo(true)
            .jsonPath("$.message").value(message -> message.toString().contains("Event has 2 active tickets out of 2 total"))
            .jsonPath("$.activeTicketCount").isEqualTo(2)
            .jsonPath("$.totalTicketCount").isEqualTo(2);
    }
    
    @Test
    public void checkTicketsForEvent_WithNoTickets_ShouldReturn200() {
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/nonexistent-event-id/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("nonexistent-event-id")
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_WithEmptyEventId_ShouldReturn404() {
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/ /check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo(" ")
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_WithNullEventId_ShouldReturn404() {
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/null/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("null")
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_WithSpecialCharactersEventId_ShouldReturn200() {
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/event-with-special-chars-123_@/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("event-with-special-chars-123_@")
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_WithVeryLongEventId_ShouldReturn200() {
        String longEventId = "very-long-event-id-" + "a".repeat(100);
        
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/{eventId}/check", longEventId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo(longEventId)
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_NoAuthenticationRequired_ShouldReturn200() {
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/public-event-id/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("public-event-id")
            .jsonPath("$.hasTickets").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("No tickets found for this event")
            .jsonPath("$.activeTicketCount").isEqualTo(0)
            .jsonPath("$.totalTicketCount").isEqualTo(0);
    }
    
    @Test
    public void checkTicketsForEvent_WithMultipleEventsData_ShouldReturnCorrectCount() {
        String token = createUserAndGetToken();
        
        TicketCreateDTO createDTO1 = new TicketCreateDTO(
            "Maria Santos",
            "87747294034",
            "maria@email.com",
            "Teste Evento",
            new BigDecimal("250.00")
        );
        
        TicketCreateDTO createDTO2 = new TicketCreateDTO(
            "João Silva",
            "87747294034",
            "joao@email.com",
            "Festival Internacional de Música e Arte Contemporânea do Brasil 2024",
            new BigDecimal("300.00")
        );
        
        TicketCreateDTO createDTO3 = new TicketCreateDTO(
            "Ana Costa",
            "87747294034",
            "ana@email.com",
            "Festival Internacional de Música e Arte Contemporânea do Brasil 2024",
            new BigDecimal("350.00")
        );
        
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO1)
            .exchange()
            .expectStatus().isCreated();
            
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO2)
            .exchange()
            .expectStatus().isCreated();
            
        webTestClient
            .post()
            .uri("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(createDTO3)
            .exchange()
            .expectStatus().isCreated();
        
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/event-test-id/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("event-test-id")
            .jsonPath("$.hasTickets").isEqualTo(true)
            .jsonPath("$.activeTicketCount").isEqualTo(1)
            .jsonPath("$.totalTicketCount").isEqualTo(1);
        
        webTestClient
            .get()
            .uri("/api/v1/tickets/event/event-long-id/check")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.eventId").isEqualTo("event-long-id")
            .jsonPath("$.hasTickets").isEqualTo(true)
            .jsonPath("$.activeTicketCount").isEqualTo(2)
            .jsonPath("$.totalTicketCount").isEqualTo(2);
    }
}
