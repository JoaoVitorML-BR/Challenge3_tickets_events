package com.jv.ticket.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

import com.jv.ticket.user.dto.UserCreateDTO;
import com.jv.ticket.user.dto.UserResponseDTO;
import com.jv.ticket.user.mapper.UserMapper;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        User user = UserMapper.toEntity(createDTO);
        User createdUser = userService.createUser(user);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(createdUser);
        return ResponseEntity.status(201).body(responseDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR ( hasRole('CLIENT') AND #id == authentication.principal.id)")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        User user = userService.getUser(id);
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userService.getAllUsers(pageable);
        Page<UserResponseDTO> responsePage = usersPage.map(UserMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }
}
