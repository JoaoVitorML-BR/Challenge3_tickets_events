package com.jv.ticket.ticket.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.models.Ticket;

public class TicketMapper {
    
    /**
     * Converte TicketCreateDTO para Ticket entity
     */
    public static Ticket toEntity(TicketCreateDTO dto, String userId) {
        if (dto == null) {
            return null;
        }
        
        Ticket ticket = new Ticket();
        ticket.setTicketId(UUID.randomUUID().toString());
        ticket.setCustomerName(dto.getCustomerName().trim());
        ticket.setCpf(dto.getCpf().trim());
        ticket.setCustomerEmail(dto.getCustomerEmail().trim());
        ticket.setEventId(dto.getEventId().trim());
        ticket.setEventName(dto.getEventName().trim());
        ticket.setBrlAmount(dto.getBrlAmount());
        ticket.setUsdAmount(dto.getUsdAmount());
        ticket.setUserId(userId);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setStatus(Ticket.TicketStatus.ACTIVE);
        
        return ticket;
    }
    
    /**
     * Converte Ticket entity para TicketResponseDTO
     */
    public static TicketResponseDTO toResponseDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setCpf(ticket.getCpf());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setBrlTotalAmount(ticket.getBrlAmount());
        dto.setUsdTotalAmount(ticket.getUsdAmount());
        dto.setStatus(ticket.getStatus().getDescription());
        
        // Criar DTO do evento
        if (ticket.getEventAddress() != null) {
            TicketResponseDTO.EventDTO eventDTO = new TicketResponseDTO.EventDTO();
            eventDTO.setEventId(ticket.getEventId());
            eventDTO.setEventName(ticket.getEventName());
            eventDTO.setEventDateTime(ticket.getEventDateTime());
            eventDTO.setLogradouro(ticket.getEventAddress().getLogradouro());
            eventDTO.setBairro(ticket.getEventAddress().getBairro());
            eventDTO.setCidade(ticket.getEventAddress().getCidade());
            eventDTO.setUf(ticket.getEventAddress().getUf());
            eventDTO.setCep(ticket.getEventAddress().getCep());
            dto.setEvent(eventDTO);
        }
        
        return dto;
    }
}
