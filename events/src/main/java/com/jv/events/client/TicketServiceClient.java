package com.jv.events.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jv.events.dto.TicketCheckResponseDTO;

@FeignClient(name = "ticket-service", url = "http://ec2-3-143-221-128.us-east-2.compute.amazonaws.com:8081", fallback = TicketServiceFallback.class)
public interface TicketServiceClient {

    @GetMapping("/api/v1/tickets/event/{eventId}/check")
    TicketCheckResponseDTO checkTicketsForEvent(@PathVariable("eventId") String eventId);
}
