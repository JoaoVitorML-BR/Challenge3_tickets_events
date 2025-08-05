package com.jv.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventUpdateDTO {

    @NotBlank
    @Size(min = 5, max = 100, message = "Event name must be between 5 and 100 characters")
    private String eventName;

    @NotBlank(message = "Date is required")
    private String eventDate;

    @NotBlank(message = "ZIP code is required")
    private String cep;
}
