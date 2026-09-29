package com.computaquest.repository;

import com.computaquest.enums.Role;
import com.computaquest.model.User;

import java.time.Instant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByRoleAndLastLoginDateAfter(Role role, Instant date);
}
