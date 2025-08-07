package com.jv.ticket.ticket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketCreateDTO createDTO) {
        log.info("Creating new ticket for event: {}", createDTO.getEventName());
        TicketResponseDTO ticket = ticketService.createTicket(createDTO);
        log.info("Ticket created successfully with ID: {}", ticket.getTicketId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/event/{eventId}/exists")
    public ResponseEntity<Boolean> hasTicketsForEvent(@PathVariable String eventId) {
        log.info("Checking if tickets exist for event: {}", eventId);
        boolean hasTickets = ticketService.hasTicketsForEvent(eventId);
        log.info("Event {} has tickets: {}", eventId, hasTickets);
        return ResponseEntity.ok(hasTickets);
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> getTicketCountForEvent(@PathVariable String eventId) {
        log.info("Getting ticket count for event: {}", eventId);
        long count = ticketService.getTicketCountForEvent(eventId);
        log.info("Event {} has {} tickets", eventId, count);
        return ResponseEntity.ok(count);
    }

}
