package com.log430.brokerx.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import com.log430.brokerx.dto.CreateUserRequest;
import com.log430.brokerx.dto.UserResponse;
import com.log430.brokerx.model.User;
import com.log430.brokerx.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUser(@Validated @RequestBody CreateUserRequest req) {
        try {
            if (userRepository.existsByEmail(req.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Un utilisateur avec cet email existe déjà"));
            }

            if (userRepository.existsByUsername(req.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Ce nom d'utilisateur est déjà pris"));
            }

            // Hash le password (optionnel, tu peux garder {noop})
            String hashedPassword = passwordEncoder.encode(req.getPassword());
            // Ou garder ton système actuel: String hashedPassword = "{noop}" + req.getPassword();
            
            // Créer et sauvegarder l'utilisateur
            User user = new User(req.getUsername(), req.getEmail(), hashedPassword);
            User savedUser = userRepository.save(user);
            
            // Retourner une réponse sans le mot de passe
            UserResponse response = new UserResponse(
                savedUser.getId(), 
                savedUser.getUsername(), 
                savedUser.getEmail()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erreur: données invalides ou conflit"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erreur interne du serveur"));
        }
    }

    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}