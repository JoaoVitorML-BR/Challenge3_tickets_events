package com.jv.ticket.ticket.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.jv.ticket.ticket.dto.EventDTO;
import com.jv.ticket.ticket.dto.EventPageResponseDTO;

@FeignClient(
    name = "event-service",
    url = "http://ec2-13-58-194-161.us-east-2.compute.amazonaws.com:8080",
    fallback = EventServiceFallback.class
)
public interface EventServiceClient {
    
    @GetMapping("/api/v1/events")
    EventPageResponseDTO getEvents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "false") boolean canceled,
        @RequestParam(defaultValue = "eventName") String sort,
        @RequestParam(defaultValue = "ASC") String direction
    );
    
    @GetMapping("/api/v1/events/{eventId}")
    EventDTO getEventById(@PathVariable String eventId);
}
