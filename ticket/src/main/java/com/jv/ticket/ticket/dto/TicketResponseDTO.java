package com.jv.ticket.ticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketResponseDTO {
    
    private String ticketId;
    private String cpf;
    private String customerName;
    private String customerEmail;
    private EventDTO event;
    private BigDecimal brlTotalAmount;
    private String status;
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class EventDTO {
        private String eventId;
        private String eventName;
        private LocalDateTime eventDateTime;
        private String logradouro;
        private String bairro;
        private String cidade;
        private String uf;
        private String cep;
    }
}
