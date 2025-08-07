package com.jv.ticket.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.jv.ticket.user.models.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);

    @Query("SELECT u.role FROM User u WHERE u.username = :username")
    User.Role findRoleByUsername(String username);
}
