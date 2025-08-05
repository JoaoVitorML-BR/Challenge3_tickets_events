package com.jv.events.models;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "events")
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    @Field("event_name")
    private String eventName;
    @Field("event_date")
    private LocalDate eventDate;
    @Field("event_cep")
    private String cep;
    @Field("event_logradouro")
    private String logradouro;
    @Field("event_bairro")
    private String bairro;
    @Field("event_cidade")
    private String cidade;
    @Field("event_uf")
    private String uf;

    @Field("event_canceled")
    private boolean canceled = false;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                '}';
    }
}
