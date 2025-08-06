package com.jv.ticket.ticket.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "tickets")
public class Ticket implements Serializable {
    
    @Id
    private String ticketId;
    
    @Field("customer_name")
    private String customerName;
    
    @Field("cpf")
    private String cpf;
    
    @Field("customer_email")
    private String customerEmail;
    
    @Field("event_id")
    private String eventId;
    
    @Field("event_name")
    private String eventName;
    
    @Field("event_date_time")
    private LocalDateTime eventDateTime;
    
    @Field("event_address")
    private EventAddress eventAddress;
    
    @Field("brl_amount")
    private BigDecimal brlAmount;
    
    @Field("status")
    private TicketStatus status = TicketStatus.ACTIVE;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("user_id")
    private String userId;
    
    public enum TicketStatus {
        ACTIVE("ativo"),
        CANCELLED("cancelado"),
        USED("utilizado");
        
        private final String description;
        
        TicketStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EventAddress implements Serializable {
        private String logradouro;
        private String bairro;
        private String cidade;
        private String uf;
        private String cep;
    }
}
