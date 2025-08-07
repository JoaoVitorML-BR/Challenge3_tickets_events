package com.jv.ticket.ticket.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.jv.ticket.ticket.models.Ticket;
import com.jv.ticket.ticket.models.Ticket.TicketStatus;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    
    List<Ticket> findByCpf(String cpf);
    
    List<Ticket> findByEventId(String eventId);
    
    List<Ticket> findByUserId(String userId);
    
    List<Ticket> findByStatus(TicketStatus status);
    
    List<Ticket> findByUserIdAndStatus(String userId, TicketStatus status);
    
    List<Ticket> findByEventIdAndStatus(String eventId, TicketStatus status);
    
    Page<Ticket> findByUserId(String userId, Pageable pageable);
    
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    Page<Ticket> findByEventId(String eventId, Pageable pageable);
    
    boolean existsByEventId(String eventId);
    
    long countByEventId(String eventId);
    
    long countByEventIdAndStatus(String eventId, TicketStatus status);
}
