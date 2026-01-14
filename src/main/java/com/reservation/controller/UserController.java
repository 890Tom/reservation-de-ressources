package com.reservation.controller;

import com.reservation.dto.request.UpdateProfileRequest;
import com.reservation.dto.request.UpdateUserRequest;
import com.reservation.dto.response.MessageResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.enums.Role;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.exception.UnauthorizedException;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * GET /api/users/me
     * Récupérer le profil de l'utilisateur connecté
     * Accessible par : USER, MANAGER, ADMIN
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        try {
            UserResponse userResponse = userService.getCurrentUserProfile();
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * PUT /api/users/me
     * Mettre à jour le profil de l'utilisateur connecté
     * Accessible par : USER, MANAGER, ADMIN
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            UserResponse userResponse = userService.updateCurrentUserProfile(request);
            return ResponseEntity.ok(userResponse);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la mise à jour du profil"));
        }
    }
    
    /**
     * GET /api/users
     * Lister tous les utilisateurs avec pagination
     * Accessible par : MANAGER, ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<UserResponse> users = userService.getAllUsers(pageable);
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * GET /api/users/{id}
     * Récupérer un utilisateur par son ID
     * Accessible par : MANAGER, ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        
        try {
            UserResponse userResponse = userService.getUserById(id);
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la récupération de l'utilisateur"));
        }
    }
    
    /**
     * PUT /api/users/{id}
     * Mettre à jour un utilisateur
     * Accessible par : ADMIN uniquement
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        
        try {
            UserResponse userResponse = userService.updateUser(id, request);
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la mise à jour de l'utilisateur"));
        }
    }
    
    /**
     * DELETE /api/users/{id}
     * Supprimer un utilisateur
     * Accessible par : ADMIN uniquement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("Utilisateur supprimé avec succès"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la suppression de l'utilisateur"));
        }
    }
    
    /**
     * PATCH /api/users/{id}/role
     * Changer le rôle d'un utilisateur
     * Accessible par : ADMIN uniquement
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        
        try {
            UserResponse userResponse = userService.changeUserRole(id, role);
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors du changement de rôle"));
        }
    }
    
    /**
     * PATCH /api/users/{id}/status
     * Activer/désactiver un utilisateur
     * Accessible par : ADMIN uniquement
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        
        try {
            UserResponse userResponse = userService.toggleUserStatus(id);
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors du changement de statut"));
        }
    }
}