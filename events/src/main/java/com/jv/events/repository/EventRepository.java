package com.jv.events.repository;

import java.util.List;
import java.util.Optional;

import com.jv.events.models.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByCanceledFalse();

    List<Event> findByCanceledTrue();

    List<Event> findByCanceled(Boolean canceled);

    List<Event> findByEventNameContainingIgnoreCase(String eventName);

    Optional<Event> findByEventNameIgnoreCase(String eventName);

    boolean existsByEventNameIgnoreCase(String eventName);
}
