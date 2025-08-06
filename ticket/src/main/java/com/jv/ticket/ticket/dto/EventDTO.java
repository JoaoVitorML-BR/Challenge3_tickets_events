package com.jv.ticket.ticket.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EventDTO {
    @JsonProperty("id")
    private String eventId;

    @JsonProperty("eventName")
    private String eventName;

    private String description;

    @JsonProperty("eventDate")
    private String eventDateString;

    @JsonProperty("canceled")
    private boolean canceled;

    @JsonProperty("cep")
    private String zipCode;

    @JsonProperty("logradouro")
    private String street;

    @JsonProperty("bairro")
    private String neighborhood;

    @JsonProperty("cidade")
    private String city;

    @JsonProperty("uf")
    private String state;

    @JsonIgnore
    public LocalDateTime getEventDate() {
        if (eventDateString != null && !eventDateString.isEmpty()) {
            try {
                return LocalDateTime.parse(eventDateString + " 00:00:00",
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @JsonIgnore
    public EventAddressDTO getAddress() {
        EventAddressDTO address = new EventAddressDTO();
        address.setStreet(this.street);
        address.setNumber("");
        address.setNeighborhood(this.neighborhood);
        address.setCity(this.city);
        address.setState(this.state);
        address.setZipCode(this.zipCode);
        return address;
    }

    @Data
    public static class EventAddressDTO {
        private String street;
        private String number;
        private String neighborhood;
        private String city;
        private String state;
        private String zipCode;
    }
}
