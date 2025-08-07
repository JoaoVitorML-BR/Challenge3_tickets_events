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
import com.jv.ticket.ticket.dto.TicketCheckResponseDTO;
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
        TicketResponseDTO ticket = ticketService.createTicket(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/event/{eventId}/exists")
    public ResponseEntity<Boolean> hasTicketsForEvent(@PathVariable String eventId) {
        boolean hasTickets = ticketService.hasTicketsForEvent(eventId);
        return ResponseEntity.ok(hasTickets);
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> getTicketCountForEvent(@PathVariable String eventId) {
        long count = ticketService.getTicketCountForEvent(eventId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/event/{eventId}/active-check")
    public ResponseEntity<TicketCheckResponseDTO> checkActiveTicketsForEvent(@PathVariable String eventId) {
        TicketCheckResponseDTO response = ticketService.checkActiveTicketsForEvent(eventId);
        return ResponseEntity.ok(response);
    }

}
