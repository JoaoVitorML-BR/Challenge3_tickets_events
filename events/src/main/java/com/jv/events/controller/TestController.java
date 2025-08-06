package com.jv.events.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.client.TicketServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final TicketServiceClient ticketServiceClient;

    @GetMapping("/tickets/event/{eventId}/exists")
    public ResponseEntity<?> testTicketExists(@PathVariable String eventId) {
        try {
            log.info("Testing ticket existence for event: {}", eventId);
            Boolean hasTickets = ticketServiceClient.hasTicketsForEvent(eventId);
            log.info("Result: Event {} has tickets: {}", eventId, hasTickets);
            
            return ResponseEntity.ok().body(new TestResponse(
                eventId, 
                hasTickets, 
                "Connection to ticket service successful"
            ));
        } catch (Exception e) {
            log.error("Error testing ticket service connection: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new TestResponse(
                eventId, 
                null, 
                "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/tickets/event/{eventId}/count")
    public ResponseEntity<?> testTicketCount(@PathVariable String eventId) {
        try {
            log.info("Testing ticket count for event: {}", eventId);
            Long count = ticketServiceClient.getTicketCountForEvent(eventId);
            log.info("Result: Event {} has {} tickets", eventId, count);
            
            return ResponseEntity.ok().body(new TestCountResponse(
                eventId, 
                count, 
                "Connection to ticket service successful"
            ));
        } catch (Exception e) {
            log.error("Error testing ticket service connection: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new TestCountResponse(
                eventId, 
                null, 
                "Error: " + e.getMessage()
            ));
        }
    }

    // Classes internas para resposta dos testes
    public static class TestResponse {
        public String eventId;
        public Boolean hasTickets;
        public String message;

        public TestResponse(String eventId, Boolean hasTickets, String message) {
            this.eventId = eventId;
            this.hasTickets = hasTickets;
            this.message = message;
        }
    }

    public static class TestCountResponse {
        public String eventId;
        public Long ticketCount;
        public String message;

        public TestCountResponse(String eventId, Long ticketCount, String message) {
            this.eventId = eventId;
            this.ticketCount = ticketCount;
            this.message = message;
        }
    }
}
