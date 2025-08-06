package com.jv.ticket.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserUpdateDTO {
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;

    @Email(message = "Invalid Format for Email")
    private String email;

    @NotBlank(message = "Password is required to confirm changes")
    private String currentPassword;
}
