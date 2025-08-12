package com.jv.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jv.events.client.ViaCepClient;
import com.jv.events.dto.ErrorResponseDTO;
import com.jv.events.dto.EventCreateDTO;
import com.jv.events.dto.EventResponseDTO;
import com.jv.events.dto.EventUpdateDTO;
import com.jv.events.dto.PagedEventResponseDTO;
import com.jv.events.dto.ViaCepResponse;
import com.jv.events.exception.CepInvalidoException;
import com.jv.events.exception.EventAlreadyCancelledException;
import com.jv.events.exception.EventCancellationNotAllowedException;
import com.jv.events.exception.EventCreationException;
import com.jv.events.exception.EventNameAlreadyExistsException;
import com.jv.events.exception.EventNotFoundException;
import com.jv.events.exception.ViaCepApiException;
import com.jv.events.mapper.EventMapper;
import com.jv.events.models.Event;
import com.jv.events.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@Tag(name = "Events", description = "API for managing events")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ViaCepClient viaCepClient;

    @Operation(summary = "Get all events", description = "Retrieves all events with optional filtering and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedEventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @Parameter(description = "Filter by canceled status") @RequestParam(value = "canceled", required = false) Boolean canceled,
            @Parameter(description = "Page number for pagination") @RequestParam(value = "page", required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(value = "sort", defaultValue = "eventDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(value = "direction", defaultValue = "ASC") String direction) {
        try {
            if (page != null) {
                Sort.Direction sortDirection = Sort.Direction.fromString(direction);
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

                Page<Event> eventPage = eventService.getEventsPaginated(canceled, pageable);

                List<EventResponseDTO> events = eventPage.getContent().stream()
                        .map(EventMapper::toResponseDTO)
                        .collect(Collectors.toList());

                PagedEventResponseDTO response = new PagedEventResponseDTO(
                        events,
                        eventPage.getNumber(),
                        eventPage.getTotalPages(),
                        eventPage.getTotalElements(),
                        eventPage.getSize(),
                        eventPage.hasNext(),
                        eventPage.hasPrevious());

                return ResponseEntity.ok(response);
            } else {
                List<Event> events = eventService.getEventsByStatus(canceled);
                List<EventResponseDTO> response = events.stream()
                        .map(EventMapper::toResponseDTO)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            throw new EventCreationException("Error when fetching events", e);
        }
    }

    @Operation(summary = "Get event by ID", description = "Retrieves a specific event by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@Parameter(description = "Event ID") @PathVariable String id) {
        try {
            Event event = eventService.getEventById(id)
                    .orElseThrow(() -> new EventNotFoundException(id));

            EventResponseDTO response = EventMapper.toResponseDTO(event);
            return ResponseEntity.ok(response);
        } catch (EventNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new EventCreationException("Error when searching for event", e);
        }
    }

    @Operation(summary = "Search events by name", description = "Searches for events by name with optional pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedEventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchEventsByName(
            @Parameter(description = "Event name to search for", required = true) @RequestParam(value = "name", required = true) String name,
            @Parameter(description = "Page number for pagination") @RequestParam(value = "page", required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(value = "sort", defaultValue = "eventDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(value = "direction", defaultValue = "ASC") String direction) {
        try {
            if (page != null) {
                Sort.Direction sortDirection = Sort.Direction.fromString(direction);
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

                Page<Event> eventPage = eventService.searchEventsByNamePaginated(name, pageable);

                List<EventResponseDTO> events = eventPage.getContent().stream()
                        .map(EventMapper::toResponseDTO)
                        .collect(Collectors.toList());

                PagedEventResponseDTO response = new PagedEventResponseDTO(
                        events,
                        eventPage.getNumber(),
                        eventPage.getTotalPages(),
                        eventPage.getTotalElements(),
                        eventPage.getSize(),
                        eventPage.hasNext(),
                        eventPage.hasPrevious());

                return ResponseEntity.ok(response);
            } else {
                List<Event> events = eventService.searchEventsByName(name);
                List<EventResponseDTO> response = events.stream()
                        .map(EventMapper::toResponseDTO)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            throw new EventCreationException("Error searching for events by name", e);
        }
    }

    @Operation(summary = "Update event", description = "Updates an existing event by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid CEP or request data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Event name already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "Event ID") @PathVariable String id,
            @Valid @RequestBody EventUpdateDTO eventUpdateDTO) {
        try {
            Event existingEvent = eventService.getEventById(id)
                    .orElseThrow(() -> new EventNotFoundException(id));

            ViaCepResponse viaCepResponse = viaCepClient.buscarCep(eventUpdateDTO.getCep());

            if (viaCepResponse.isErro()) {
                throw new CepInvalidoException(eventUpdateDTO.getCep());
            }

            Event updatedEvent = EventMapper.toEntityForUpdate(eventUpdateDTO, viaCepResponse, existingEvent);
            Event savedEvent = eventService.updateEvent(id, updatedEvent);
            EventResponseDTO response = EventMapper.toResponseDTO(savedEvent);

            return ResponseEntity.ok(response);
        } catch (EventNotFoundException | CepInvalidoException | EventNameAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            if (e.getMessage().contains("ViaCEP") || e.getMessage().contains("CEP")) {
                throw new ViaCepApiException("Communication failure with CEP service", e);
            } else {
                throw new EventCreationException("Failed to update event", e);
            }
        }
    }

    @Operation(summary = "Cancel event", description = "Cancels an event by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event cancelled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Event cancellation not allowed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Event already cancelled", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(@Parameter(description = "Event ID") @PathVariable String id) {
        try {
            Event canceledEvent = eventService.cancelEvent(id);

            if (canceledEvent == null) {
                throw new EventNotFoundException(id);
            }

            EventResponseDTO response = EventMapper.toResponseDTO(canceledEvent);
            return ResponseEntity.ok(response);
        } catch (EventNotFoundException | EventCancellationNotAllowedException | EventAlreadyCancelledException e) {
            throw e;
        } catch (Exception e) {
            throw new EventCreationException("Error canceling event", e);
        }
    }

    @Operation(summary = "Reactivate event", description = "Reactivates a cancelled event by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event reactivated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<EventResponseDTO> reactivateEvent(
            @Parameter(description = "Event ID") @PathVariable String id) {
        try {
            Event reactivatedEvent = eventService.reactivateEvent(id);

            if (reactivatedEvent == null) {
                throw new EventNotFoundException(id);
            }

            EventResponseDTO response = EventMapper.toResponseDTO(reactivatedEvent);
            return ResponseEntity.ok(response);
        } catch (EventNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new EventCreationException("Error reactivating event", e);
        }
    }

    @Operation(summary = "Create event", description = "Creates a new event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid CEP or request data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Event name already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
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

        } catch (CepInvalidoException | EventNameAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            if (e.getMessage().contains("ViaCEP") || e.getMessage().contains("CEP")) {
                throw new ViaCepApiException("Communication failure with CEP service", e);
            } else {
                throw new EventCreationException("Failed to process event data", e);
            }
        }
    }
}
