package com.log430.brokerx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.log430.brokerx.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
     
    boolean existsByUsername(String username);
    
    User findByEmail(String email);
    
    User findByUsername(String username);
}