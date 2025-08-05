package com.jv.events.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventResponseDTO {
    
    private String id;
    private String eventName;
    private String dateTime;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private boolean canceled;
}
