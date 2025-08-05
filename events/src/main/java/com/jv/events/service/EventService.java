package com.jv.events.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jv.events.models.Event;
import com.jv.events.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }
    
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }
    
    public Event updateEvent(String id, Event updatedEvent) {
        updatedEvent.setId(id);
        return eventRepository.save(updatedEvent);
    }
    
    public Event cancelEvent(String id) {
        Optional<Event> eventOptional = getEventById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            event.setCanceled(true);
            return eventRepository.save(event);
        }
        return null;
    }
    
    public Event reactivateEvent(String id) {
        Optional<Event> eventOptional = getEventById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            event.setCanceled(false);
            return eventRepository.save(event);
        }
        return null;
    }
}
