package com.reservation.controller.mvc;

import com.reservation.dto.request.UpdateProfileRequest;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.service.ReservationService;
import com.reservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserMvcController {
    
    private final UserService userService;
    private final ReservationService reservationService;
    
    public UserMvcController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }
    
    /**
     * Dashboard utilisateur
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        
        try {
            // Récupérer les informations de l'utilisateur
            UserResponse currentUser = userService.getCurrentUserProfile();
            
            // Récupérer les réservations de l'utilisateur
            List<ReservationResponse> myReservations = reservationService.getMyReservations();
            
            // Filtrer les réservations à venir
            List<ReservationResponse> upcomingReservations = myReservations.stream()
                .filter(r -> r.getStartTime().isAfter(java.time.LocalDateTime.now()))
                .limit(5)
                .toList();
            
            model.addAttribute("pageTitle", "Dashboard");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("totalReservations", myReservations.size());
            model.addAttribute("upcomingReservations", upcomingReservations);
            
            return "user/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement du dashboard");
            return "user/dashboard";
        }
    }
    
    /**
     * Page de profil
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        
        try {
            UserResponse currentUser = userService.getCurrentUserProfile();
            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
            updateProfileRequest.setEmail(currentUser.getEmail());
            updateProfileRequest.setFirstName(currentUser.getFirstName());
            updateProfileRequest.setLastName(currentUser.getLastName());
            updateProfileRequest.setDepartment(currentUser.getDepartment());
            
            model.addAttribute("pageTitle", "Mon Profil");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("updateProfileRequest", updateProfileRequest);
            
            return "user/profile";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement du profil");
            return "user/profile";
        }
    }
    
    /**
     * Mise à jour du profil
     */
    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute UpdateProfileRequest updateProfileRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        
        if (bindingResult.hasErrors()) {
            UserResponse currentUser = userService.getCurrentUserProfile();
            model.addAttribute("pageTitle", "Mon Profil");
            model.addAttribute("currentUser", currentUser);
            return "user/profile";
        }
        
        try {
            userService.updateCurrentUserProfile(updateProfileRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour avec succès !");
            return "redirect:/profile";
            
        } catch (UserAlreadyExistsException e) {
            UserResponse currentUser = userService.getCurrentUserProfile();
            model.addAttribute("pageTitle", "Mon Profil");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("errorMessage", e.getMessage());
            return "user/profile";
        } catch (Exception e) {
            UserResponse currentUser = userService.getCurrentUserProfile();
            model.addAttribute("pageTitle", "Mon Profil");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour du profil");
            return "user/profile";
        }
    }
    
    /**
     * Mes réservations
     */
    @GetMapping("/my-reservations")
    public String myReservations(Model model) {
        
        try {
            List<ReservationResponse> myReservations = reservationService.getMyReservations();
            
            model.addAttribute("pageTitle", "Mes Réservations");
            model.addAttribute("reservations", myReservations);
            
            return "user/my-reservations";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des réservations");
            return "user/my-reservations";
        }
    }
}