package com.jv.events.client;

import org.springframework.stereotype.Component;

import com.jv.events.dto.TicketCheckResponseDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TicketServiceFallback implements TicketServiceClient {

    @Override
    public TicketCheckResponseDTO checkTicketsForEvent(String eventId) {
        log.warn("Ticket service unavailable for event {}, using fallback", eventId);
        return new TicketCheckResponseDTO(
            eventId, 
            false, 
            "Ticket service unavailable - assuming no active tickets", 
            0L, 
            0L
        );
    }
}
