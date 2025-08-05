package com.jv.events.repository;

import java.util.Optional;

import com.jv.events.models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    Page<Event> findByCanceled(Boolean canceled, Pageable pageable);
    
    Page<Event> findByEventNameContainingIgnoreCase(String eventName, Pageable pageable);

    Optional<Event> findByEventNameIgnoreCase(String eventName);

    boolean existsByEventNameIgnoreCase(String eventName);
}
