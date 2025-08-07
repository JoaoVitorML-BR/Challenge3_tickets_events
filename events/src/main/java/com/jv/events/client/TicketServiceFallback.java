package com.jv.events.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TicketServiceFallback implements TicketServiceClient {

    @Override
    public Boolean hasTicketsForEvent(String eventId) {
        log.warn("Fallback: Unable to check if tickets exist for event: {}. Assuming no tickets exist.", eventId);
        return false;
    }

    @Override
    public Long getTicketCountForEvent(String eventId) {
        log.warn("Fallback: Unable to get ticket count for event: {}. Returning 0.", eventId);
        return 0L;
    }
}
