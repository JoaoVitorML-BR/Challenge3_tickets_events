package com.jv.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.client.ViaCepClient;
import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;
import com.jv.events.dto.ViaCepResponse;
import com.jv.events.exception.CepInvalidoException;
import com.jv.events.exception.EventCreationException;
import com.jv.events.exception.ViaCepApiException;
import com.jv.events.mapper.EventMapper;
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
    private final ViaCepClient viaCepClient;

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
