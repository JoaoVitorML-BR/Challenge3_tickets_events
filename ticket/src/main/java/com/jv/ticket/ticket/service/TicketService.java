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
        log.info("Creating ticket for customer: {} and event: {}", 
                createDTO.getCustomerName(), createDTO.getEventName());
        
        CpfValidator.validateCpf(createDTO.getCpf());
        
        String userId = getCurrentUserId();
        
        EventDTO event = validateAndGetEvent(createDTO.getEventName());
        
        Ticket ticket = TicketMapper.toEntity(createDTO, userId, event);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        log.info("Ticket created successfully with ID: {}", savedTicket.getTicketId());
        
        return TicketMapper.toResponseDTO(savedTicket);
    }
    
    private EventDTO validateAndGetEvent(String eventName) {
        try {
            log.info("Validating event: {}", eventName);
            
            EventPageResponseDTO eventPage = eventServiceClient.getEvents(0, false, "eventName", "ASC");
            
            log.info("Event page response received: events={}, totalElements={}", 
                    eventPage.getEvents() != null ? eventPage.getEvents().size() : "null", 
                    eventPage.getTotalElements());
            
            if (eventPage.getEvents() == null || eventPage.getEvents().isEmpty()) {
                throw new EventNotFoundException("No events found or event service returned empty response");
            }
            
            EventDTO event = eventPage.getEvents().stream()
                    .filter(e -> e.getEventName().equalsIgnoreCase(eventName))
                    .findFirst()
                    .orElseThrow(() -> new EventNotFoundException("Event '" + eventName + "' not found or is cancelled"));
            
            log.info("Event found: {} - ID: {} - Cancelled: {}", 
                    event.getEventName(), event.getEventId(), event.isCanceled());
            return event;
            
        } catch (Exception ex) {
            log.error("Error validating event '{}': {}", eventName, ex.getMessage());
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
}
