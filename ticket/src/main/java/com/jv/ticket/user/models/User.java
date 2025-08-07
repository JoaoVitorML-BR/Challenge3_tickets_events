package com.jv.ticket.user.models;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class User implements Serializable {
    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("cpf")
    private String cpf;

    @Field("role")
    private Role role = Role.ROLE_CLIENT;

    public enum Role {
        ROLE_ADMIN,
        ROLE_CLIENT
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
