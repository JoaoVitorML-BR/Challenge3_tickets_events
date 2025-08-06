package com.jv.ticket.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.jv.ticket.user.models.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Format for Email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max=25, message = "Password must be between 6 and 25 characters")
    private String password;
    
    private User.Role role;
}
