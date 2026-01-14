package com.reservation.service.impl;

import com.reservation.dto.request.UpdateProfileRequest;
import com.reservation.dto.request.UpdateUserRequest;
import com.reservation.dto.response.UserResponse;
import com.reservation.entity.User;
import com.reservation.enums.Role;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.exception.UnauthorizedException;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.repository.UserRepository;
import com.reservation.security.service.UserDetailsImpl;
import com.reservation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserResponse getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        logger.info("Récupération du profil de l'utilisateur: {}", currentUser.getUsername());
        return toUserResponse(currentUser);
    }
    
    @Override
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        logger.info("Mise à jour du profil de l'utilisateur: {}", currentUser.getUsername());
        
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Cet email est déjà utilisé");
            }
            currentUser.setEmail(request.getEmail());
        }
        
        // Mettre à jour le mot de passe si fourni
        if (StringUtils.hasText(request.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Mettre à jour les autres champs
        if (StringUtils.hasText(request.getFirstName())) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            currentUser.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            currentUser.setDepartment(request.getDepartment());
        }
        
        User updatedUser = userRepository.save(currentUser);
        logger.info("Profil mis à jour avec succès pour: {}", updatedUser.getUsername());
        
        return toUserResponse(updatedUser);
    }
    
    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        logger.info("Récupération de tous les utilisateurs (page: {})", pageable.getPageNumber());
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }
    
    @Override
    public UserResponse getUserById(Long id) {
        logger.info("Récupération de l'utilisateur avec l'ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        return toUserResponse(user);
    }
    
    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        logger.info("Mise à jour de l'utilisateur avec l'ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Vérifier si l'email est déjà utilisé
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }
        
        // Mettre à jour les champs
        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("Utilisateur {} mis à jour avec succès", updatedUser.getUsername());
        
        return toUserResponse(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        logger.info("Suppression de l'utilisateur avec l'ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Ne pas permettre la suppression de son propre compte
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("Vous ne pouvez pas supprimer votre propre compte");
        }
        
        userRepository.delete(user);
        logger.info("Utilisateur {} supprimé avec succès", user.getUsername());
    }
    
    @Override
    public UserResponse changeUserRole(Long id, Role role) {
        logger.info("Changement du rôle de l'utilisateur {} vers {}", id, role);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Ne pas permettre de changer son propre rôle
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("Vous ne pouvez pas changer votre propre rôle");
        }
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        
        logger.info("Rôle de l'utilisateur {} changé vers {}", updatedUser.getUsername(), role);
        
        return toUserResponse(updatedUser);
    }
    
    @Override
    public UserResponse toggleUserStatus(Long id) {
        logger.info("Changement du statut de l'utilisateur: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Ne pas permettre de désactiver son propre compte
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("Vous ne pouvez pas désactiver votre propre compte");
        }
        
        user.setActive(!user.getActive());
        User updatedUser = userRepository.save(user);
        
        logger.info("Statut de l'utilisateur {} changé vers: {}", 
            updatedUser.getUsername(), updatedUser.getActive() ? "actif" : "inactif");
        
        return toUserResponse(updatedUser);
    }
    
    @Override
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDepartment(user.getDepartment());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
    
    
    // Récupère l'utilisateur actuellement connecté
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}