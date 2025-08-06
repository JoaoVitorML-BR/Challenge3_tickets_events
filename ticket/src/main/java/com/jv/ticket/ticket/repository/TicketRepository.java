package com.jv.ticket.ticket.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.jv.ticket.ticket.models.Ticket;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    
    // Buscar tickets por CPF
    List<Ticket> findByCpf(String cpf);
    
    // Buscar tickets por eventId
    List<Ticket> findByEventId(String eventId);
    
    // Buscar tickets por userId
    List<Ticket> findByUserId(String userId);
    
    // Verificar se existem tickets para um evento
    boolean existsByEventId(String eventId);
    
    // Contar tickets por eventId
    long countByEventId(String eventId);
}
