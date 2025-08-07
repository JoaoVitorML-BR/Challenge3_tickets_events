package com.jv.events.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jv.events.dto.TicketCheckResponseDTO;

@FeignClient(name = "ticket-service", url = "http://localhost:8081", fallback = TicketServiceFallback.class)
public interface TicketServiceClient {

    @GetMapping("/api/v1/tickets/event/{eventId}/exists")
    Boolean hasTicketsForEvent(@PathVariable("eventId") String eventId);

    @GetMapping("/api/v1/tickets/event/{eventId}/count")
    Long getTicketCountForEvent(@PathVariable("eventId") String eventId);

    @GetMapping("/api/v1/tickets/event/{eventId}/active-check")
    TicketCheckResponseDTO checkActiveTicketsForEvent(@PathVariable("eventId") String eventId);
}
