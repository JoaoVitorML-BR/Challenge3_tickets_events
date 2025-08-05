package com.jv.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {

    private String id;
    private String eventName;
    private String eventDate;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private boolean canceled;
}
