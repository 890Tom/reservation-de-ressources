package com.reservation.service;

import com.reservation.dto.request.UpdateProfileRequest;
import com.reservation.dto.request.UpdateUserRequest;
import com.reservation.dto.response.UserResponse;
import com.reservation.entity.User;
import com.reservation.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    

    // Récupérer le profil de l'utilisateur connecté
    UserResponse getCurrentUserProfile();
    
    
    // Mettre à jour le profil de l'utilisateur connecté
    UserResponse updateCurrentUserProfile(UpdateProfileRequest request);
    

    // Lister tous les utilisateurs (avec pagination)
    Page<UserResponse> getAllUsers(Pageable pageable);
    

    // Récupérer un utilisateur par son ID
    UserResponse getUserById(Long id);
    
    
    // Mettre à jour un utilisateur (ADMIN)
    UserResponse updateUser(Long id, UpdateUserRequest request);
    
    // Supprimer un utilisateur (ADMIN)
    void deleteUser(Long id);
    
    // Changer le rôle d'un utilisateur (ADMIN)
    UserResponse changeUserRole(Long id, Role role);
    
    // Activer/désactiver un utilisateur (ADMIN)
    UserResponse toggleUserStatus(Long id);
    
    // Convertir User en UserResponse
    UserResponse toUserResponse(User user);
}