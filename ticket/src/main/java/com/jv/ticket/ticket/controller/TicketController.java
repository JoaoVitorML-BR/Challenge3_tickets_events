package com.jv.ticket.ticket.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.dto.TicketCheckResponseDTO;
import com.jv.ticket.ticket.models.Ticket;
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

    @GetMapping("/event/{eventId}/check")
    public ResponseEntity<TicketCheckResponseDTO> checkTicketsForEvent(@PathVariable String eventId) {
        TicketCheckResponseDTO response = ticketService.checkActiveTicketsForEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable String ticketId) {
        Optional<TicketResponseDTO> ticket = ticketService.getTicketById(ticketId);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<TicketResponseDTO>> getMyTickets(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketResponseDTO> tickets = ticketService.getMyTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByStatus(@PathVariable Ticket.TicketStatus status) {
        List<TicketResponseDTO> tickets = ticketService.getTicketsByStatus(status);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{ticketId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TicketResponseDTO> cancelTicket(@PathVariable String ticketId) {
        TicketResponseDTO cancelledTicket = ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok(cancelledTicket);
    }

}
