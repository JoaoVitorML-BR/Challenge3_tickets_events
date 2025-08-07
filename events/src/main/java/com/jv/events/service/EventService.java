package com.jv.events.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jv.events.client.TicketServiceClient;
import com.jv.events.dto.TicketCheckResponseDTO;
import com.jv.events.exception.EventCancellationNotAllowedException;
import com.jv.events.exception.EventNameAlreadyExistsException;
import com.jv.events.models.Event;
import com.jv.events.repository.EventRepository;
import com.jv.events.util.DateUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final TicketServiceClient ticketServiceClient;

    public Event createEvent(Event event) {
        if (eventRepository.existsByEventNameIgnoreCase(event.getEventName())) {
            throw new EventNameAlreadyExistsException(event.getEventName());
        }

        if (!DateUtil.isValidFutureDate(DateUtil.formatDate(event.getEventDate()))) {
            throw new IllegalArgumentException("Event date must be in the future and in dd/MM/yyyy format");
        }

        return eventRepository.save(event);
    }

    public Page<Event> getEventsPaginated(Boolean canceled, Pageable pageable) {
        if (canceled == null) {
            return eventRepository.findAll(pageable);
        }
        return eventRepository.findByCanceled(canceled, pageable);
    }

    public Page<Event> searchEventsByNamePaginated(String name, Pageable pageable) {
        return eventRepository.findByEventNameContainingIgnoreCase(name, pageable);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByStatus(Boolean canceled) {
        if (canceled == null) {
            return eventRepository.findAll();
        }
        return eventRepository.findByCanceled(canceled, Pageable.unpaged()).getContent();
    }

    public List<Event> searchEventsByName(String name) {
        return eventRepository.findByEventNameContainingIgnoreCase(name, Pageable.unpaged()).getContent();
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public Event updateEvent(String id, Event updatedEvent) {
        Optional<Event> existingEventWithSameName = eventRepository
                .findByEventNameIgnoreCase(updatedEvent.getEventName());
        if (existingEventWithSameName.isPresent() && !existingEventWithSameName.get().getId().equals(id)) {
            throw new EventNameAlreadyExistsException(updatedEvent.getEventName());
        }

        updatedEvent.setId(id);
        return eventRepository.save(updatedEvent);
    }

    public Event cancelEvent(String id) {
        Optional<Event> eventOptional = getEventById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            try {
                TicketCheckResponseDTO ticketCheck = ticketServiceClient.checkActiveTicketsForEvent(id);
                if (ticketCheck.isHasTickets() && ticketCheck.getActiveTicketCount() > 0) {
                    throw new EventCancellationNotAllowedException(id, ticketCheck.getActiveTicketCount());
                }

                if (ticketCheck.getTotalTicketCount() > 0 && ticketCheck.getActiveTicketCount() == 0) {
                    log.info("Event {} has {} total tickets but all are cancelled. Proceeding with event cancellation.",
                            id, ticketCheck.getTotalTicketCount());
                } else {
                    log.info("Event {} has no tickets, proceeding with cancellation", id);
                }

            } catch (EventCancellationNotAllowedException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Failed to check tickets for event {}: {}. Proceeding with cancellation due to fallback.",
                        id, e.getMessage());
                throw new RuntimeException("Failed to check tickets for event " + id + ": " + e.getMessage());
            }

            event.setCanceled(true);
            Event savedEvent = eventRepository.save(event);
            log.info("Event {} canceled successfully", id);
            return savedEvent;
        }
        log.warn("Event with ID {} not found for cancellation", id);
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
