package com.jv.ticket.ticket.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jv.ticket.ticket.client.EventServiceClient;
import com.jv.ticket.ticket.dto.EventDTO;
import com.jv.ticket.ticket.dto.EventPageResponseDTO;
import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.dto.TicketCheckResponseDTO;
import com.jv.ticket.ticket.exception.EventNotFoundException;
import com.jv.ticket.ticket.mapper.TicketMapper;
import com.jv.ticket.ticket.models.Ticket;
import com.jv.ticket.ticket.repository.TicketRepository;
import com.jv.ticket.ticket.util.CpfValidator;
import com.jv.ticket.user.jwt.JwtUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventServiceClient eventServiceClient;

    @Transactional
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO) {
        CpfValidator.validateCpf(createDTO.getCpf());

        String userId = getCurrentUserId();

        EventDTO event = validateAndGetEvent(createDTO.getEventName());

        Ticket ticket = TicketMapper.toEntity(createDTO, userId, event);

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketMapper.toResponseDTO(savedTicket);
    }

    private EventDTO validateAndGetEvent(String eventName) {
        try {
            EventPageResponseDTO eventPage = eventServiceClient.getEvents(0, false, "eventName", "ASC");

            if (eventPage.getEvents() == null || eventPage.getEvents().isEmpty()) {
                throw new EventNotFoundException("No events found or event service returned empty response");
            }

            EventDTO event = eventPage.getEvents().stream()
                    .filter(e -> e.getEventName().equalsIgnoreCase(eventName))
                    .findFirst()
                    .orElseThrow(
                            () -> new EventNotFoundException("Event '" + eventName + "' not found or is cancelled"));
            return event;

        } catch (Exception ex) {
            if (ex instanceof EventNotFoundException) {
                throw ex;
            }
            throw new EventNotFoundException("Unable to validate event '" + eventName + "'. Please try again later.");
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    public boolean hasTicketsForEvent(String eventId) {
        return ticketRepository.existsByEventId(eventId);
    }

    public long getTicketCountForEvent(String eventId) {
        return ticketRepository.countByEventId(eventId);
    }

    public TicketCheckResponseDTO checkActiveTicketsForEvent(String eventId) {
        try {
            long totalTickets = ticketRepository.countByEventId(eventId);
            long activeTickets = totalTickets;
            boolean hasTickets = totalTickets > 0;

            String message = hasTickets
                    ? String.format("Event has %d active tickets out of %d total", activeTickets, totalTickets)
                    : "No tickets found for this event";

            return new TicketCheckResponseDTO(eventId, hasTickets, message, activeTickets, totalTickets);

        } catch (Exception e) {
            return new TicketCheckResponseDTO(
                    eventId,
                    false,
                    "Error checking tickets: " + e.getMessage(),
                    0L,
                    0L);
        }
    }
}
