package com.log430.brokerx.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.brokerx.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
