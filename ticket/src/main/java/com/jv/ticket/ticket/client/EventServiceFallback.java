package com.jv.ticket.ticket.client;

import org.springframework.stereotype.Component;

import com.jv.ticket.ticket.dto.EventDTO;
import com.jv.ticket.ticket.dto.EventPageResponseDTO;
import com.jv.ticket.ticket.exception.EventServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventServiceFallback implements EventServiceClient {
    
    @Override
    public EventPageResponseDTO getEvents(int page, boolean canceled, String sort, String direction) {
        log.error("Event service is unavailable - falling back");
        throw new EventServiceUnavailableException("Event service is currently unavailable. Please try again later.");
    }
    
    @Override
    public EventDTO getEventById(String eventId) {
        log.error("Event service is unavailable - falling back for event ID: {}", eventId);
        throw new EventServiceUnavailableException("Event service is currently unavailable. Please try again later.");
    }
}
