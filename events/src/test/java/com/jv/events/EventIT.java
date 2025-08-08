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

    @Test
    @DisplayName("Should get all events without pagination")
    void getAllEvents_WithoutPagination_ShouldReturnEventsList() throws Exception {
        Event event1 = new Event();
        event1.setEventName("Event One");
        event1.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        event1.setCep("01001000");
        event1.setLogradouro("Praça da Sé");
        event1.setBairro("Sé");
        event1.setCidade("São Paulo");
        event1.setUf("SP");
        event1.setCanceled(false);
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setEventName("Event Two");
        event2.setEventDate(java.time.LocalDate.of(2025, 12, 26));
        event2.setCep("01001000");
        event2.setLogradouro("Praça da Sé");
        event2.setBairro("Sé");
        event2.setCidade("São Paulo");
        event2.setUf("SP");
        event2.setCanceled(true);
        eventRepository.save(event2);

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Event One"));
            assertTrue(responseBody.contains("Event Two"));
            assertTrue(responseBody.contains("São Paulo"));
        }
    }

    @Test
    @DisplayName("Should get events with pagination")
    void getAllEvents_WithPagination_ShouldReturnPagedResponse() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Event event = new Event();
            event.setEventName("Event " + i);
            event.setEventDate(java.time.LocalDate.of(2025, 12, i + 20));
            event.setCep("01001000");
            event.setLogradouro("Praça da Sé");
            event.setBairro("Sé");
            event.setCidade("São Paulo");
            event.setUf("SP");
            event.setCanceled(false);
            eventRepository.save(event);
        }

        String url = baseUrl + "?page=0&size=3";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("totalPages"));
            assertTrue(responseBody.contains("totalElements"));
            assertTrue(responseBody.contains("hasNext"));
            assertTrue(responseBody.contains("hasPrevious"));
        }
    }

    @Test
    @DisplayName("Should filter events by canceled status")
    void getAllEvents_FilterByCanceled_ShouldReturnFilteredEvents() throws Exception {
        Event activeEvent = new Event();
        activeEvent.setEventName("Active Event");
        activeEvent.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        activeEvent.setCep("01001000");
        activeEvent.setLogradouro("Praça da Sé");
        activeEvent.setBairro("Sé");
        activeEvent.setCidade("São Paulo");
        activeEvent.setUf("SP");
        activeEvent.setCanceled(false);
        eventRepository.save(activeEvent);

        Event canceledEvent = new Event();
        canceledEvent.setEventName("Canceled Event");
        canceledEvent.setEventDate(java.time.LocalDate.of(2025, 12, 26));
        canceledEvent.setCep("01001000");
        canceledEvent.setLogradouro("Praça da Sé");
        canceledEvent.setBairro("Sé");
        canceledEvent.setCidade("São Paulo");
        canceledEvent.setUf("SP");
        canceledEvent.setCanceled(true);
        eventRepository.save(canceledEvent);

        String url = baseUrl + "?canceled=false";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Active Event"));
            assertFalse(responseBody.contains("Canceled Event"));
        }
    }

    // ========================== GET EVENT BY ID TESTS ==========================

    @Test
    @DisplayName("Should get event by ID successfully")
    void getEventById_ValidId_ShouldReturnEvent() throws Exception {
        Event savedEvent = new Event();
        savedEvent.setEventName("Tech Meetup 2025");
        savedEvent.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        savedEvent.setCep("01001000");
        savedEvent.setLogradouro("Praça da Sé");
        savedEvent.setBairro("Sé");
        savedEvent.setCidade("São Paulo");
        savedEvent.setUf("SP");
        savedEvent.setCanceled(false);
        Event event = eventRepository.save(savedEvent);

        String url = baseUrl + "/" + event.getId();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Tech Meetup 2025"));
            assertTrue(responseBody.contains("01001000"));
            assertTrue(responseBody.contains("Praça da Sé"));
            assertTrue(responseBody.contains("São Paulo"));
            assertTrue(responseBody.contains(event.getId()));
        }
    }

    @Test
    @DisplayName("Should return 404 when getting event with non-existent ID")
    void getEventById_NonExistentId_ShouldReturn404() throws Exception {
        String nonExistentId = "507f1f77bcf86cd799439011";

        String url = baseUrl + "/" + nonExistentId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Evento Não Encontrado"));
            assertTrue(responseBody.contains(nonExistentId));
        }
    }

    @Test
    @DisplayName("Should get canceled event by ID successfully")
    void getEventById_CanceledEvent_ShouldReturnEvent() throws Exception {
        Event savedEvent = new Event();
        savedEvent.setEventName("Canceled Conference");
        savedEvent.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        savedEvent.setCep("01001000");
        savedEvent.setLogradouro("Praça da Sé");
        savedEvent.setBairro("Sé");
        savedEvent.setCidade("São Paulo");
        savedEvent.setUf("SP");
        savedEvent.setCanceled(true);
        Event event = eventRepository.save(savedEvent);

        String url = baseUrl + "/" + event.getId();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Canceled Conference"));
            assertTrue(responseBody.contains("true"));
            assertTrue(responseBody.contains(event.getId()));
        }
    }

    @Test
    @DisplayName("Should return 400 when getting event with invalid ID format")
    void getEventById_InvalidIdFormat_ShouldReturn400OrError() throws Exception {
        String invalidId = "invalid-id-format";

        String url = baseUrl + "/" + invalidId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        assertNotNull(response.getBody());
    }

    // ========================== SEARCH EVENTS BY NAME TESTS ==========================

    @Test
    @DisplayName("Should search events by name without pagination")
    void searchEventsByName_WithoutPagination_ShouldReturnMatchingEvents() throws Exception {
        Event techEvent = new Event();
        techEvent.setEventName("Tech Conference 2025");
        techEvent.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        techEvent.setCep("01001000");
        techEvent.setLogradouro("Praça da Sé");
        techEvent.setBairro("Sé");
        techEvent.setCidade("São Paulo");
        techEvent.setUf("SP");
        techEvent.setCanceled(false);
        eventRepository.save(techEvent);

        Event musicEvent = new Event();
        musicEvent.setEventName("Music Festival 2025");
        musicEvent.setEventDate(java.time.LocalDate.of(2025, 12, 26));
        musicEvent.setCep("01001000");
        musicEvent.setLogradouro("Praça da Sé");
        musicEvent.setBairro("Sé");
        musicEvent.setCidade("São Paulo");
        musicEvent.setUf("SP");
        musicEvent.setCanceled(false);
        eventRepository.save(musicEvent);

        String url = baseUrl + "/search?name=Tech";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Tech Conference 2025"));
            assertFalse(responseBody.contains("Music Festival 2025"));
        }
    }

    @Test
    @DisplayName("Should search events by name with pagination")
    void searchEventsByName_WithPagination_ShouldReturnPagedResults() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Event event = new Event();
            event.setEventName("Conference " + i);
            event.setEventDate(java.time.LocalDate.of(2025, 12, i + 20));
            event.setCep("01001000");
            event.setLogradouro("Praça da Sé");
            event.setBairro("Sé");
            event.setCidade("São Paulo");
            event.setUf("SP");
            event.setCanceled(false);
            eventRepository.save(event);
        }

        String url = baseUrl + "/search?name=Conference&page=0&size=3";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("Conference"));
            assertTrue(responseBody.contains("totalPages"));
            assertTrue(responseBody.contains("totalElements"));
            assertTrue(responseBody.contains("hasNext"));
            assertTrue(responseBody.contains("hasPrevious"));
        }
    }

    @Test
    @DisplayName("Should return empty list when no events match search term")
    void searchEventsByName_NoMatches_ShouldReturnEmptyList() throws Exception {
        Event event = new Event();
        event.setEventName("Tech Conference 2025");
        event.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        event.setCep("01001000");
        event.setLogradouro("Praça da Sé");
        event.setBairro("Sé");
        event.setCidade("São Paulo");
        event.setUf("SP");
        event.setCanceled(false);
        eventRepository.save(event);

        String url = baseUrl + "/search?name=NonExistentEvent";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("[]") || responseBody.contains("\"events\":[]"));
        }
    }

    @Test
    @DisplayName("Should search events case-insensitively")
    void searchEventsByName_CaseInsensitive_ShouldReturnMatchingEvents() throws Exception {
        Event event = new Event();
        event.setEventName("TECH Conference 2025");
        event.setEventDate(java.time.LocalDate.of(2025, 12, 25));
        event.setCep("01001000");
        event.setLogradouro("Praça da Sé");
        event.setBairro("Sé");
        event.setCidade("São Paulo");
        event.setUf("SP");
        event.setCanceled(false);
        eventRepository.save(event);

        String url = baseUrl + "/search?name=tech";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String responseBody = response.getBody();
        if (responseBody != null) {
            assertTrue(responseBody.contains("TECH Conference 2025"));
        }
    }
}
