package com.jv.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.client.ViaCepClient;
import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;
import com.jv.events.dto.ViaCepResponse;
import com.jv.events.exception.CepInvalidoException;
import com.jv.events.exception.EventCreationException;
import com.jv.events.exception.EventNotFoundException;
import com.jv.events.exception.ViaCepApiException;
import com.jv.events.mapper.EventMapper;
import com.jv.events.models.Event;
import com.jv.events.service.EventService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    private final ViaCepClient viaCepClient;

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            List<EventResponseDTO> response = events.stream()
                .map(EventMapper::toResponseDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new EventCreationException("Erro ao buscar eventos", e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable String id) {
        try {
            Event event = eventService.getEventById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
            
            EventResponseDTO response = EventMapper.toResponseDTO(event);
            return ResponseEntity.ok(response);
        } catch (EventNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new EventCreationException("Erro ao buscar evento", e);
        }
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO) {
        
        try {
            ViaCepResponse viaCepResponse = viaCepClient.buscarCep(eventCreateDTO.getCep());
            
            if (viaCepResponse.isErro()) {
                throw new CepInvalidoException(eventCreateDTO.getCep());
            }

            Event event = EventMapper.toEntity(eventCreateDTO, viaCepResponse);
            Event savedEvent = eventService.createEvent(event);
            EventResponseDTO response = EventMapper.toResponseDTO(savedEvent);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (CepInvalidoException e) {
            throw e;
        } catch (Exception e) {            
            if (e.getMessage().contains("ViaCEP") || e.getMessage().contains("CEP")) {
                throw new ViaCepApiException("Falha na comunicação com serviço de CEP", e);
            } else {
                throw new EventCreationException("Falha ao processar dados do evento", e);
            }
        }
    }
}
