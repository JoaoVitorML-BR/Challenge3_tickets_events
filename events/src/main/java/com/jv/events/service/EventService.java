package com.jv.events.service;

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
}
