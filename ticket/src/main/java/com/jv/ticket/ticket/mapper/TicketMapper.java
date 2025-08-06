package com.jv.ticket.ticket.mapper;

import java.time.LocalDateTime;

import com.jv.ticket.ticket.dto.EventDTO;
import com.jv.ticket.ticket.dto.TicketCreateDTO;
import com.jv.ticket.ticket.dto.TicketResponseDTO;
import com.jv.ticket.ticket.models.Ticket;

public class TicketMapper {
    public static Ticket toEntity(TicketCreateDTO dto, String userId, EventDTO event) {
        if (dto == null) {
            return null;
        }
        
        Ticket ticket = new Ticket();
        ticket.setCustomerName(dto.getCustomerName().trim());
        ticket.setCpf(dto.getCpf().trim());
        ticket.setCustomerEmail(dto.getCustomerEmail().trim());
        ticket.setEventId(event.getEventId());
        ticket.setEventName(event.getEventName());
        ticket.setEventDateTime(event.getEventDate());
        ticket.setBrlAmount(dto.getBrlAmount());
        ticket.setUserId(userId);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setStatus(Ticket.TicketStatus.ACTIVE);
        
        if (event.getAddress() != null) {
            Ticket.EventAddress eventAddress = new Ticket.EventAddress();
            eventAddress.setLogradouro(event.getAddress().getStreet() + ", " + event.getAddress().getNumber());
            eventAddress.setBairro(event.getAddress().getNeighborhood());
            eventAddress.setCidade(event.getAddress().getCity());
            eventAddress.setUf(event.getAddress().getState());
            eventAddress.setCep(event.getAddress().getZipCode());
            ticket.setEventAddress(eventAddress);
        }
        
        return ticket;
    }
    
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
        dto.setStatus(ticket.getStatus().getDescription());
        
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
