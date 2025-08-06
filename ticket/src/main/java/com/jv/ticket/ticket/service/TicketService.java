package com.jv.ticket.ticket.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.mapper.TicketMapper;
import com.jv.ticket.ticket.models.Ticket;
import com.jv.ticket.ticket.repository.TicketRepository;
import com.jv.ticket.ticket.util.CpfValidator;
import com.jv.ticket.user.jwt.JwtUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final TicketRepository ticketRepository;
    
    @Transactional
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO) {
        log.info("Creating ticket for customer: {} and event: {}", 
                createDTO.getCustomerName(), createDTO.getEventName());
        
        // Validar CPF
        CpfValidator.validateCpf(createDTO.getCpf());
        
        // Obter ID do usuário autenticado
        String userId = getCurrentUserId();
        
        // TODO: Validar se o evento existe no ms-event-manager
        // TODO: Buscar dados completos do evento (endereço, data/hora)
        
        // Converter DTO para entidade
        Ticket ticket = TicketMapper.toEntity(createDTO, userId);
        
        // Salvar no banco
        Ticket savedTicket = ticketRepository.save(ticket);
        
        log.info("Ticket created successfully with ID: {}", savedTicket.getTicketId());
        
        // Converter para DTO de resposta
        return TicketMapper.toResponseDTO(savedTicket);
    }
    
    /**
     * Obter ID do usuário autenticado
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new RuntimeException("User not authenticated");
    }
}
