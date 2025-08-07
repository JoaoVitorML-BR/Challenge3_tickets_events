package com.jv.ticket.ticket.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.jv.ticket.ticket.exception.TicketNotFoundException;
import com.jv.ticket.ticket.exception.TicketAlreadyCancelledException;
import com.jv.ticket.ticket.exception.UnauthorizedTicketAccessException;
import com.jv.ticket.ticket.exception.CpfMismatchException;
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
        String userCpf = getCurrentUserCpf();
        
        String formattedInputCpf = CpfValidator.formatCpf(createDTO.getCpf());
        String formattedUserCpf = CpfValidator.formatCpf(userCpf);
        
        if (!formattedInputCpf.equals(formattedUserCpf)) {
            throw new CpfMismatchException("The provided CPF does not match your registered CPF");
        }
        
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

    private String getCurrentUserCpf() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            return userDetails.getCpf();
        }
        throw new RuntimeException("User not authenticated");
    }

    public TicketCheckResponseDTO checkActiveTicketsForEvent(String eventId) {
        try {
            long totalTickets = ticketRepository.countByEventId(eventId);
            long activeTickets = ticketRepository.countByEventIdAndStatus(eventId, Ticket.TicketStatus.ACTIVE);
            boolean hasTickets = activeTickets > 0;
            
            String message = totalTickets == 0 
                ? "No tickets found for this event"
                : String.format("Event has %d active tickets out of %d total", activeTickets, totalTickets);
            
            return new TicketCheckResponseDTO(eventId, hasTickets, message, activeTickets, totalTickets);
            
        } catch (Exception e) {
            return new TicketCheckResponseDTO(
                eventId, 
                false, 
                "Error checking tickets: " + e.getMessage(), 
                0L, 
                0L
            );
        }
    }

    public Optional<TicketResponseDTO> getTicketById(String ticketId) {
        return ticketRepository.findById(ticketId)
                .map(TicketMapper::toResponseDTO);
    }

    public Page<TicketResponseDTO> getTicketsByUser(String userId, Pageable pageable) {
        return ticketRepository.findByUserId(userId, pageable)
                .map(TicketMapper::toResponseDTO);
    }

    public Page<TicketResponseDTO> getMyTickets(Pageable pageable) {
        String userId = getCurrentUserId();
        return getTicketsByUser(userId, pageable);
    }

    public List<TicketResponseDTO> getTicketsByStatus(Ticket.TicketStatus status) {
        return ticketRepository.findByStatus(status).stream()
                .map(TicketMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public TicketResponseDTO cancelTicket(String ticketId) {        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));
        
        String currentUserId = getCurrentUserId();
        if (!ticket.getUserId().equals(currentUserId)) {
            throw new UnauthorizedTicketAccessException("You don't have permission to cancel this ticket");
        }
        
        if (ticket.getStatus() == Ticket.TicketStatus.CANCELLED) {
            throw new TicketAlreadyCancelledException("Ticket is already cancelled");
        }
        
        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        return TicketMapper.toResponseDTO(savedTicket);
    }

    public List<TicketResponseDTO> getTicketsByCpf(String cpf) {
        return ticketRepository.findByCpf(cpf).stream()
                .map(TicketMapper::toResponseDTO)
                .toList();
    }
}
