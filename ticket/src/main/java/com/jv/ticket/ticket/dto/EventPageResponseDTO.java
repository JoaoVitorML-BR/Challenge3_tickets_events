package com.jv.ticket.ticket.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EventPageResponseDTO {
    @JsonProperty("events")
    private List<EventDTO> events;
    
    @JsonProperty("currentPage")
    private int currentPage;
    
    @JsonProperty("pageSize")
    private int pageSize;
    
    @JsonProperty("totalElements")
    private long totalElements;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    @JsonProperty("hasNext")
    private boolean hasNext;
    
    @JsonProperty("hasPrevious")
    private boolean hasPrevious;
    
    public List<EventDTO> getContent() {
        return events;
    }
}
