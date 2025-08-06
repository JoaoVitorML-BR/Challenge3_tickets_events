package com.jv.ticket.user.dto;

import com.jv.ticket.user.models.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private User.Role role;
}
