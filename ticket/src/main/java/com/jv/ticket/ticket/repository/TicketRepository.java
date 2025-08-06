package com.jv.ticket.ticket.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.jv.ticket.ticket.models.Ticket;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    
    List<Ticket> findByCpf(String cpf);
    
    List<Ticket> findByEventId(String eventId);
    
    List<Ticket> findByUserId(String userId);
    
    boolean existsByEventId(String eventId);
    
    long countByEventId(String eventId);
}
