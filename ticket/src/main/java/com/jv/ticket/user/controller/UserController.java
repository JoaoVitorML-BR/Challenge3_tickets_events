package com.jv.ticket.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

import com.jv.ticket.user.dto.UserCreateDTO;
import com.jv.ticket.user.dto.UserResponseDTO;
import com.jv.ticket.user.dto.UserUpdateDTO;
import com.jv.ticket.user.mapper.UserMapper;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User Management", description = "Operations related to user management")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided data.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "422", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        User user = UserMapper.toEntity(createDTO);
        User createdUser = userService.createUser(user);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(createdUser);
        return ResponseEntity.status(201).body(responseDTO);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves the details of a specific user by their ID.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR ( hasRole('CLIENT') AND #id == authentication.principal.id)")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        User user = userService.getUser(id);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Get user by CPF", description = "Retrieves the details of a specific user by their CPF.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserByCpf(@PathVariable String cpf) {
        User user = userService.getUserByCpf(cpf);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of users.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userService.getAllUsers(pageable);
        Page<UserResponseDTO> responsePage = usersPage.map(UserMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Update user by ID", description = "Updates user details using the provided data.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR ( hasRole('CLIENT') AND #id == authentication.principal.id)")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        User updatedUser = userService.updateUser(id, updateDTO);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }
}