package com.reservation.controller.mvc;

import com.reservation.dto.request.UpdateReservationRequest;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.exception.InvalidReservationException;
import com.reservation.exception.ReservationConflictException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.exception.UnauthorizedException;
import com.reservation.service.ReservationService;
import com.reservation.service.ResourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationMvcController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationMvcController.class);
    
    private final ReservationService reservationService;
    private final ResourceService resourceService;
    
    public ReservationMvcController(ReservationService reservationService,
                                   ResourceService resourceService) {
        this.reservationService = reservationService;
        this.resourceService = resourceService;
    }
    
    /**
     * Formulaire de modification de réservation
     */
    @GetMapping("/{id}/edit")
    public String editReservationForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Formulaire de modification de la réservation: {}", id);
        
        try {
            ReservationResponse reservation = reservationService.getReservationById(id);
            ResourceResponse resource = resourceService.getResourceById(reservation.getResourceId());
            
            // Vérifier que la réservation est modifiable (PENDING uniquement)
            if (!"PENDING".equals(reservation.getStatus().name())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Seules les réservations en attente peuvent être modifiées");
                return "redirect:/my-reservations";
            }
            
            UpdateReservationRequest updateRequest = new UpdateReservationRequest();
            updateRequest.setStartTime(reservation.getStartTime());
            updateRequest.setEndTime(reservation.getEndTime());
            updateRequest.setPurpose(reservation.getPurpose());
            
            model.addAttribute("pageTitle", "Modifier la réservation");
            model.addAttribute("reservation", reservation);
            model.addAttribute("resource", resource);
            model.addAttribute("updateReservationRequest", updateRequest);
            
            return "reservation/edit";
            
        } catch (ResourceNotFoundException e) {
            logger.error("Réservation non trouvée: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Réservation non trouvée");
            return "redirect:/my-reservations";
        } catch (UnauthorizedException e) {
            logger.error("Accès non autorisé: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Vous n'avez pas la permission de modifier cette réservation");
            return "redirect:/my-reservations";
        }
    }
    
    /**
     * Traitement modification de réservation
     */
    @PostMapping("/{id}/edit")
    public String editReservation(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateReservationRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        logger.info("Modification de la réservation: {}", id);
        
        if (bindingResult.hasErrors()) {
            try {
                ReservationResponse reservation = reservationService.getReservationById(id);
                ResourceResponse resource = resourceService.getResourceById(reservation.getResourceId());
                
                model.addAttribute("pageTitle", "Modifier la réservation");
                model.addAttribute("reservation", reservation);
                model.addAttribute("resource", resource);
                return "reservation/edit";
            } catch (Exception e) {
                return "redirect:/my-reservations";
            }
        }
        
        try {
            reservationService.updateReservation(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Réservation modifiée avec succès !");
            return "redirect:/my-reservations";
            
        } catch (ResourceNotFoundException e) {
            logger.error("Réservation non trouvée: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Réservation non trouvée");
            return "redirect:/my-reservations";
        } catch (UnauthorizedException e) {
            logger.error("Accès non autorisé");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-reservations";
        } catch (InvalidReservationException | ReservationConflictException e) {
            logger.error("Erreur de modification: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reservations/" + id + "/edit";
        } catch (Exception e) {
            logger.error("Erreur inattendue", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification");
            return "redirect:/my-reservations";
        }
    }
    
    /**
     * Annuler une réservation
     */
    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Annulation de la réservation: {}", id);
        
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Réservation annulée avec succès !");
            return "redirect:/my-reservations";
            
        } catch (ResourceNotFoundException e) {
            logger.error("Réservation non trouvée: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Réservation non trouvée");
            return "redirect:/my-reservations";
        } catch (UnauthorizedException e) {
            logger.error("Accès non autorisé");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-reservations";
        } catch (InvalidReservationException e) {
            logger.error("Annulation impossible: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-reservations";
        } catch (Exception e) {
            logger.error("Erreur inattendue", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'annulation");
            return "redirect:/my-reservations";
        }
    }
}