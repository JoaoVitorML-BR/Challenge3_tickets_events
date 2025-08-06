package com.jv.ticket.ticket.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketCreateDTO {
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "CPF is required")
    private String cpf;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotBlank(message = "Event name is required")
    private String eventName;
    
    @NotNull(message = "BRL amount is required")
    @DecimalMin(value = "0.01", message = "BRL amount must be greater than 0")
    private BigDecimal brlAmount;
    
    @NotNull(message = "USD amount is required")
    @DecimalMin(value = "0.01", message = "USD amount must be greater than 0")
    private BigDecimal usdAmount;
}
