package com.jv.events.mapper;

import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;
import com.jv.events.dto.EventUpdateDTO;
import com.jv.events.dto.ViaCepResponse;
import com.jv.events.models.Event;

public class EventMapper {
    
    public static Event toEntity(EventCreateDTO dto, ViaCepResponse viaCepResponse) {
        Event event = new Event();
        event.setEventName(dto.getEventName());
        event.setDateTime(dto.getDateTime());
        event.setCep(viaCepResponse.getCep());
        event.setLogradouro(viaCepResponse.getLogradouro());
        event.setBairro(viaCepResponse.getBairro());
        event.setCidade(viaCepResponse.getLocalidade());
        event.setUf(viaCepResponse.getUf());
        event.setCanceled(false);
        return event;
    }
    
    public static Event toEntityForUpdate(EventUpdateDTO dto, ViaCepResponse viaCepResponse, Event existingEvent) {
        Event event = new Event();
        event.setId(existingEvent.getId());
        event.setEventName(dto.getEventName());
        event.setDateTime(dto.getDateTime());
        event.setCep(viaCepResponse.getCep());
        event.setLogradouro(viaCepResponse.getLogradouro());
        event.setBairro(viaCepResponse.getBairro());
        event.setCidade(viaCepResponse.getLocalidade());
        event.setUf(viaCepResponse.getUf());
        event.setCanceled(existingEvent.isCanceled());
        return event;
    }
    
    public static EventResponseDTO toResponseDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setDateTime(event.getDateTime());
        dto.setCep(event.getCep());
        dto.setLogradouro(event.getLogradouro());
        dto.setBairro(event.getBairro());
        dto.setCidade(event.getCidade());
        dto.setUf(event.getUf());
        dto.setCanceled(event.isCanceled());
        return dto;
    }
}
