package com.jv.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;
import com.jv.events.models.Event;
import com.jv.events.service.EventService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO) {
        try {
            Event event = new Event();
            event.setEventName(eventCreateDTO.getEventName());
            event.setDateTime(eventCreateDTO.getDateTime());
            event.setLocation(eventCreateDTO.getCep());
            event.setCanceled(false);
            
            Event savedEvent = eventService.createEvent(event);
            
            EventResponseDTO eventResponseDTO = new EventResponseDTO();
            eventResponseDTO.setId(savedEvent.getId());
            eventResponseDTO.setEventName(savedEvent.getEventName());
            eventResponseDTO.setDateTime(savedEvent.getDateTime());
            eventResponseDTO.setCep(savedEvent.getLocation());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(eventResponseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
