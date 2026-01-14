package com.reservation.service;

import com.reservation.dto.request.LoginRequest;
import com.reservation.dto.request.RegisterRequest;
import com.reservation.dto.response.AuthResponse;
import com.reservation.entity.User;

public interface AuthService {
    
    // Authentifie un utilisateur et génère un token JWT
    AuthResponse login(LoginRequest loginRequest);
    
    
    // Enregistre un nouvel utilisateur
    User register(RegisterRequest registerRequest);
    
    // Vérifie si un username existe déjà
    boolean existsByUsername(String username);
    
    //Vérifie si un email existe déjà
    boolean existsByEmail(String email);
}