package com.reservation.controller.mvc;

import com.reservation.dto.request.CreateReservationRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import com.reservation.exception.InvalidReservationException;
import com.reservation.exception.ReservationConflictException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.service.ReservationService;
import com.reservation.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/resources")
public class ResourceMvcController {
    
    private final ResourceService resourceService;
    private final ReservationService reservationService;
    
    public ResourceMvcController(ResourceService resourceService, ReservationService reservationService) {
        this.resourceService = resourceService;
        this.reservationService = reservationService;
    }
    
    /**
     * Liste des ressources avec filtres
     */
    @GetMapping
    public String listResources(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model
    ) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<ResourceResponse> resources = resourceService.getAllResources(type, status, pageable);
            
            model.addAttribute("pageTitle", "Ressources disponibles");
            model.addAttribute("resources", resources);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", resources.getTotalPages());
            model.addAttribute("selectedType", type);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("resourceTypes", ResourceType.values());
            model.addAttribute("resourceStatuses", ResourceStatus.values());
            
            return "resource/list";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des ressources");
            return "resource/list";
        }
    }
    
    /**
     * Détail d'une ressource
     */
    @GetMapping("/{id}")
    public String resourceDetail(
            @PathVariable Long id,
            Model model
    ) {
        
        try {
            ResourceResponse resource = resourceService.getResourceById(id);
            List<ReservationResponse> reservations = reservationService.getReservationsByResource(id);
            
            model.addAttribute("pageTitle", resource.getName());
            model.addAttribute("resource", resource);
            model.addAttribute("reservations", reservations);
            model.addAttribute("createReservationRequest", new CreateReservationRequest());
            
            return "resource/detail";
            
        } catch (ResourceNotFoundException e) {
            return "redirect:/resources?error=notfound";
        } catch (Exception e) {
            return "redirect:/resources?error=general";
        }
    }
    
    /**
     * Créer une réservation depuis le détail de la ressource
     */
    @PostMapping("/{id}/reserve")
    public String createReservation(
            @PathVariable Long id,
            @ModelAttribute CreateReservationRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        
        // Validation manuelle
        if (request.getStartTime() == null || request.getEndTime() == null) {
            try {
                ResourceResponse resource = resourceService.getResourceById(id);
                List<ReservationResponse> reservations = reservationService.getReservationsByResource(id);
                
                model.addAttribute("pageTitle", resource.getName());
                model.addAttribute("resource", resource);
                model.addAttribute("reservations", reservations);
                model.addAttribute("createReservationRequest", request);
                model.addAttribute("errorMessage", "Les dates de début et de fin sont obligatoires");
                return "resource/detail";
            } catch (Exception e) {
                return "redirect:/resources/" + id;
            }
        }
        
        try {
            ReservationResponse response = reservationService.createReservation(request);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Réservation créée avec succès ! En attente de confirmation.");
            return "redirect:/my-reservations";
            
        } catch (InvalidReservationException | ReservationConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/resources/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de la création de la réservation: " + e.getMessage());
            return "redirect:/resources/" + id;
        }
    }
    
    /**
     * Vérifier la disponibilité (AJAX)
     */
    @GetMapping("/{id}/check-availability")
    @ResponseBody
    public AvailabilityResponse checkAvailability(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            return resourceService.checkAvailability(id, start, end);
        } catch (Exception e) {
            throw e;
        }
    }

    
}