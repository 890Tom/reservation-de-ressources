package com.reservation.service.impl;

import com.reservation.dto.request.LoginRequest;
import com.reservation.dto.request.RegisterRequest;
import com.reservation.dto.response.AuthResponse;
import com.reservation.entity.User;
import com.reservation.enums.Role;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.repository.UserRepository;
import com.reservation.security.jwt.JwtUtils;
import com.reservation.security.service.UserDetailsImpl;
import com.reservation.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    public AuthServiceImpl(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }
    
    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());
        
        // Authentification
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Génération du token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Récupération des détails de l'utilisateur
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        logger.info("Connexion réussie pour l'utilisateur: {}", userDetails.getUsername());
        
        return new AuthResponse(
            jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            userDetails.getAuthorities().iterator().next().getAuthority()
        );
    }
    
    @Override
    public User register(RegisterRequest registerRequest) {
        logger.info("Tentative d'inscription pour l'utilisateur: {}", registerRequest.getUsername());
        
        // Vérifier si le username existe déjà
        if (existsByUsername(registerRequest.getUsername())) {
            logger.warn("Le nom d'utilisateur {} existe déjà", registerRequest.getUsername());
            throw new UserAlreadyExistsException("Le nom d'utilisateur est déjà utilisé");
        }
        
        // Vérifier si l'email existe déjà
        if (existsByEmail(registerRequest.getEmail())) {
            logger.warn("L'email {} existe déjà", registerRequest.getEmail());
            throw new UserAlreadyExistsException("L'email est déjà utilisé");
        }
        
        // Créer le nouvel utilisateur
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setDepartment(registerRequest.getDepartment());
        user.setRole(Role.USER); // Par défaut, nouveau utilisateur = USER
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        logger.info("Utilisateur {} enregistré avec succès", savedUser.getUsername());
        
        return savedUser;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}