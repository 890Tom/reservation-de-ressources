package com.reservation.controller.mvc;

import com.reservation.dto.request.CreateResourceRequest;
import com.reservation.dto.request.UpdateResourceRequest;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.service.ReservationService;
import com.reservation.service.ResourceService;
import com.reservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminMvcController {
    
    private final ResourceService resourceService;
    private final ReservationService reservationService;
    private final UserService userService;
    
    public AdminMvcController(ResourceService resourceService,
                             ReservationService reservationService,
                             UserService userService) {
        this.resourceService = resourceService;
        this.reservationService = reservationService;
        this.userService = userService;
    }
    
    /**
     * Liste des ressources (ADMIN/MANAGER)
     */
    @GetMapping("/resources")
    public String manageResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<ResourceResponse> resources = resourceService.getAllResources(null, null, pageable);
            
            model.addAttribute("pageTitle", "Gestion des Ressources");
            model.addAttribute("resources", resources);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", resources.getTotalPages());
            
            return "admin/resources";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des ressources");
            return "admin/resources";
        }
    }
    
    /**
     * Formulaire de création de ressource (ADMIN/MANAGER)
     */
    @GetMapping("/resources/create")
    public String createResourceForm(Model model) {
        
        model.addAttribute("pageTitle", "Créer une Ressource");
        model.addAttribute("createResourceRequest", new CreateResourceRequest());
        model.addAttribute("resourceTypes", ResourceType.values());
        model.addAttribute("resourceStatuses", ResourceStatus.values());
        
        return "admin/resource-form";
    }
    
    /**
     * Traitement création de ressource (ADMIN/MANAGER)
     */
    @PostMapping("/resources/create")
    public String createResource(
            @Valid @ModelAttribute CreateResourceRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Créer une Ressource");
            model.addAttribute("resourceTypes", ResourceType.values());
            model.addAttribute("resourceStatuses", ResourceStatus.values());
            return "admin/resource-form";
        }
        
        try {
            resourceService.createResource(request);
            redirectAttributes.addFlashAttribute("successMessage", "Ressource créée avec succès !");
            return "redirect:/admin/resources";
            
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Créer une Ressource");
            model.addAttribute("resourceTypes", ResourceType.values());
            model.addAttribute("resourceStatuses", ResourceStatus.values());
            model.addAttribute("errorMessage", "Erreur lors de la création de la ressource");
            return "admin/resource-form";
        }
    }
    
    /**
     * Formulaire de modification de ressource (ADMIN/MANAGER)
     */
    @GetMapping("/resources/{id}/edit")
    public String editResourceForm(@PathVariable Long id, Model model) {
        
        try {
            ResourceResponse resource = resourceService.getResourceById(id);
            
            UpdateResourceRequest updateRequest = new UpdateResourceRequest();
            updateRequest.setName(resource.getName());
            updateRequest.setDescription(resource.getDescription());
            updateRequest.setType(resource.getType());
            updateRequest.setStatus(resource.getStatus());
            updateRequest.setLocation(resource.getLocation());
            updateRequest.setCapacity(resource.getCapacity());
            
            model.addAttribute("pageTitle", "Modifier la Ressource");
            model.addAttribute("resource", resource);
            model.addAttribute("updateResourceRequest", updateRequest);
            model.addAttribute("resourceTypes", ResourceType.values());
            model.addAttribute("resourceStatuses", ResourceStatus.values());
            
            return "admin/resource-edit";
            
        } catch (ResourceNotFoundException e) {
            return "redirect:/admin/resources?error=notfound";
        }
    }
    
    /**
     * Traitement modification de ressource (ADMIN/MANAGER)
     */
    @PostMapping("/resources/{id}/edit")
    public String editResource(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateResourceRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        
        if (bindingResult.hasErrors()) {
            try {
                ResourceResponse resource = resourceService.getResourceById(id);
                model.addAttribute("pageTitle", "Modifier la Ressource");
                model.addAttribute("resource", resource);
                model.addAttribute("resourceTypes", ResourceType.values());
                model.addAttribute("resourceStatuses", ResourceStatus.values());
                return "admin/resource-edit";
            } catch (Exception e) {
                return "redirect:/admin/resources";
            }
        }
        
        try {
            resourceService.updateResource(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Ressource modifiée avec succès !");
            return "redirect:/admin/resources";
            
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée");
            return "redirect:/admin/resources";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification");
            return "redirect:/admin/resources";
        }
    }
    
    /**
     * Suppression de ressource (ADMIN uniquement)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/resources/{id}/delete")
    public String deleteResource(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        try {
            resourceService.deleteResource(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ressource supprimée avec succès !");
            return "redirect:/admin/resources";
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/resources";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée");
            return "redirect:/admin/resources";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression");
            return "redirect:/admin/resources";
        }
    }
    
    /**
     * Toutes les réservations (MANAGER/ADMIN)
     */
    @GetMapping("/reservations")
    public String manageReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
            Page<ReservationResponse> reservations = reservationService.getAllReservations(pageable);
            
            model.addAttribute("pageTitle", "Gestion des Réservations");
            model.addAttribute("reservations", reservations);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reservations.getTotalPages());
            
            return "admin/reservations";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des réservations");
            return "admin/reservations";
        }
    }
    
    /**
     * Confirmer une réservation (MANAGER/ADMIN)
     */
    @PostMapping("/reservations/{id}/confirm")
    public String confirmReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        try {
            reservationService.confirmReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Réservation confirmée avec succès !");
            return "redirect:/admin/reservations";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la confirmation");
            return "redirect:/admin/reservations";
        }
    }
    
    /**
     * Annuler une réservation (MANAGER/ADMIN)
     */
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservationAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Réservation annulée avec succès !");
            return "redirect:/admin/reservations";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/reservations";
        }
    }
    
    /**
     * Liste des utilisateurs (ADMIN uniquement)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
            Page<UserResponse> users = userService.getAllUsers(pageable);
            
            model.addAttribute("pageTitle", "Gestion des Utilisateurs");
            model.addAttribute("users", users);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", users.getTotalPages());
            
            return "admin/users";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des utilisateurs");
            return "admin/users";
        }
    }
}