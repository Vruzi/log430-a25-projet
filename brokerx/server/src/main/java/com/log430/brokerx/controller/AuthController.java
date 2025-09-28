package com.log430.brokerx.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.log430.brokerx.dto.LoginRequest;
import com.log430.brokerx.dto.LoginResponse;
import com.log430.brokerx.dto.UserResponse;
import com.log430.brokerx.model.User;
import com.log430.brokerx.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("L'email est requis"));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Le mot de passe est requis"));
            }

            User user = userRepository.findByEmail(loginRequest.getEmail());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Email ou mot de passe incorrect"));
            }

            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
            
            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Email ou mot de passe incorrect"));
            }

            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
            );
            
            LoginResponse loginResponse = new LoginResponse(
                "Connexion réussie",
                userResponse,
                generateSimpleToken(user)
            );

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erreur interne du serveur"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Pour l'instant, simple réponse
        // Plus tard tu pourras invalider le token ici
        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Token manquant"));
        }
        
        // TODO: Valider le token et retourner l'utilisateur
        return ResponseEntity.ok(new MessageResponse("Endpoint à implémenter avec JWT"));
    }

    @GetMapping("/status")
    public ResponseEntity<MessageResponse> getAuthStatus() {
        return ResponseEntity.ok(new MessageResponse("Service d'authentification actif"));
    }

    private String generateSimpleToken(User user) {
        // Pour l'instant, token simple (à remplacer par JWT plus tard)
        return "simple_token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    // Classes pour les réponses
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

    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) {
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