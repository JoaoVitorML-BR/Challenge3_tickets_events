package com.jv.ticket.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketCheckResponseDTO {
    private String eventId;
    private boolean hasTickets;
    private String message;
    private long activeTicketCount;
    private long totalTicketCount;
}
