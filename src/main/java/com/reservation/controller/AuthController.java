package com.reservation.controller;

import com.reservation.dto.request.LoginRequest;
import com.reservation.dto.request.RegisterRequest;
import com.reservation.dto.response.AuthResponse;
import com.reservation.dto.response.MessageResponse;
import com.reservation.entity.User;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * POST /api/auth/register
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Requête d'inscription reçue pour: {}", registerRequest.getUsername());
        
        try {
            User user = authService.register(registerRequest);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Utilisateur enregistré avec succès : " + user.getUsername()));
                
        } catch (UserAlreadyExistsException e) {
            logger.error("Erreur lors de l'inscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'inscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de l'inscription"));
        }
    }
    
    /**
     * POST /api/auth/login
     * Connexion et génération du token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Requête de connexion reçue pour: {}", loginRequest.getUsername());
        
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            
            return ResponseEntity.ok(authResponse);
            
        } catch (BadCredentialsException e) {
            logger.error("Identifiants incorrects pour: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Nom d'utilisateur ou mot de passe incorrect"));
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la connexion"));
        }
    }
    
    /**
     * GET /api/auth/test
     * Endpoint de test (à supprimer en production)
     */
    // @GetMapping("/test")
    // public ResponseEntity<MessageResponse> test() {
    //     return ResponseEntity.ok(new MessageResponse("Auth API fonctionne correctement !"));
    // }
}