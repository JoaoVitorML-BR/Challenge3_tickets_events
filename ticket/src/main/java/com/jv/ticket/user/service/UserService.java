package com.jv.ticket.user.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jv.ticket.user.exception.EmailUniqueViolationException;
import com.jv.ticket.user.exception.EmptyDataException;
import com.jv.ticket.user.exception.UserNotFoundException;
import com.jv.ticket.user.exception.UsernameUniqueViolationException;
import com.jv.ticket.user.exception.WrongPasswordException;
import com.jv.ticket.user.dto.UserUpdateDTO;
import com.jv.ticket.user.mapper.UserMapper;
import com.jv.ticket.user.models.User;
import com.jv.ticket.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameUniqueViolationException(String.format("Username '%s' is already in use",
                    user.getUsername()));
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UsernameUniqueViolationException(String.format("Email '%s' is already in use",
                    user.getEmail()));
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public User updateUser(String id, UserUpdateDTO userUpdateDTO) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (userUpdateDTO.getCurrentPassword() == null || userUpdateDTO.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password must be provided to update user information.");
        }

        if (!passwordEncoder.matches(userUpdateDTO.getCurrentPassword(), userToUpdate.getPassword())) {
            throw new WrongPasswordException("Current password does not match.");
        }

        UserMapper.updateFromDTO(userUpdateDTO, userToUpdate);

        if (userUpdateDTO.getUsername() != null) {
            String usernameTrimmed = userUpdateDTO.getUsername().trim();
            if (usernameTrimmed.isBlank()) {
                throw new EmptyDataException("Username cannot be empty or blank.");
            }
            Optional<User> existingUserWithUsername = userRepository.findByUsername(usernameTrimmed);
            if (existingUserWithUsername.isPresent() && !existingUserWithUsername.get().getId().equals(id)) {
                throw new UsernameUniqueViolationException("Username '" + usernameTrimmed + "' is already in use.");
            }
            userToUpdate.setUsername(usernameTrimmed);
        }

        if (userUpdateDTO.getEmail() != null) {
            String emailTrimmed = userUpdateDTO.getEmail().trim();
            if (emailTrimmed.isBlank()) {
                throw new EmptyDataException("Email cannot be empty or blank.");
            }
            Optional<User> existingUserWithEmail = userRepository.findByEmail(emailTrimmed);
            if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(id)) {
                throw new EmailUniqueViolationException("Email '" + emailTrimmed + "' is already in use.");
            }
            userToUpdate.setEmail(emailTrimmed);
        }
        return userRepository.save(userToUpdate);
    }

    public User updatePassword(String id, String oldPassword, String newPassword, String confirmNewPassword) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!passwordEncoder.matches(oldPassword, userToUpdate.getPassword())) {
            throw new WrongPasswordException("Old password does not match.");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new WrongPasswordException("New password and confirm new password do not match.");
        }

        if (newPassword.isBlank()) {
            throw new WrongPasswordException("New password cannot be empty or blank.");
        }

        userToUpdate.setPassword(passwordEncoder.encode(newPassword.trim()));
        return userRepository.save(userToUpdate);
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public User.Role getRoleByUsername(String username) {
        return userRepository.findRoleByUsername(username);
    }
}
