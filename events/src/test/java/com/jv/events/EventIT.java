package com.jv.events;

import com.jv.events.client.ViaCepClient;
import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.ViaCepResponse;
import com.jv.events.models.Event;
import com.jv.events.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Event Integration Tests")
@SuppressWarnings("deprecation")
public class EventIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private ViaCepClient viaCepClient;

    private ViaCepResponse validViaCepResponse;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/events";
        eventRepository.deleteAll();
        
        validViaCepResponse = new ViaCepResponse();
        validViaCepResponse.setCep("01001000");
        validViaCepResponse.setLogradouro("Praça da Sé");
        validViaCepResponse.setBairro("Sé");
        validViaCepResponse.setLocalidade("São Paulo");
        validViaCepResponse.setUf("SP");
        validViaCepResponse.setErro(false);
    }

    // ========================== SUCCESS CASES ==========================

    @Test
    @DisplayName("Should create event successfully with valid data")
    void createEvent_ValidData_ShouldReturnCreated() throws Exception {
        EventCreateDTO createDTO = new EventCreateDTO();
        createDTO.setEventName("Tech Conference 2025");
        createDTO.setEventDate("25/12/2025");
        createDTO.setCep("01001000");

        when(viaCepClient.buscarCep(anyString())).thenReturn(validViaCepResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Tech Conference 2025"));
            assertTrue(responseBody.contains("01001000"));
            assertTrue(responseBody.contains("Praça da Sé"));
            assertTrue(responseBody.contains("São Paulo"));
        }

        assertEquals(1, eventRepository.count());
        Event savedEvent = eventRepository.findAll().get(0);
        assertEquals("Tech Conference 2025", savedEvent.getEventName());
        assertEquals("01001000", savedEvent.getCep());
        assertFalse(savedEvent.isCanceled());
    }

    // ========================== EXCEPTION TESTS ==========================

    @Test
    @DisplayName("Should return 400 when CEP is invalid")
    void createEvent_InvalidCep_ShouldReturn400() throws Exception {
        // Given
        EventCreateDTO createDTO = new EventCreateDTO();
        createDTO.setEventName("Tech Conference 2025");
        createDTO.setEventDate("25/12/2025");
        createDTO.setCep("00000000");

        ViaCepResponse invalidCepResponse = new ViaCepResponse();
        invalidCepResponse.setErro(true);
        when(viaCepClient.buscarCep(anyString())).thenReturn(invalidCepResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("CEP Inválido"));
        }

        assertEquals(0, eventRepository.count());
    }

    @Test
    @DisplayName("Should return 409 when event name already exists")
    void createEvent_DuplicateName_ShouldReturn409() throws Exception {
        Event existingEvent = new Event();
        existingEvent.setEventName("Tech Conference 2025");
        existingEvent.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        existingEvent.setCep("01001000");
        existingEvent.setLogradouro("Praça da Sé");
        existingEvent.setBairro("Sé");
        existingEvent.setCidade("São Paulo");
        existingEvent.setUf("SP");
        eventRepository.save(existingEvent);

        EventCreateDTO createDTO = new EventCreateDTO();
        createDTO.setEventName("Tech Conference 2025");
        createDTO.setEventDate("25/12/2025");
        createDTO.setCep("01001000");

        when(viaCepClient.buscarCep(anyString())).thenReturn(validViaCepResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Nome de Evento Duplicado"));
        }

        assertEquals(1, eventRepository.count());
    }

    @Test
    @DisplayName("Should return 404 when event not found")
    void getEvent_NonExistentId_ShouldReturn404() throws Exception {
        String nonExistentId = "507f1f77bcf86cd799439011";

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/" + nonExistentId, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Evento Não Encontrado"));
        }
    }
}
