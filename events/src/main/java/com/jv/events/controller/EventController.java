package com.jv.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO) {
        EventResponseDTO eventResponseDTO = new EventResponseDTO();
        eventResponseDTO.setId("1");
        eventResponseDTO.setEventName(eventCreateDTO.getEventName());
        eventResponseDTO.setDateTime(eventCreateDTO.getDateTime());
        eventResponseDTO.setCep(eventCreateDTO.getCep());
        return ResponseEntity.ok(eventResponseDTO);
    }
}
