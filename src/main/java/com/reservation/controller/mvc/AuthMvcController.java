package com.reservation.controller.mvc;

import com.reservation.dto.request.RegisterRequest;
import com.reservation.entity.User;
import com.reservation.exception.UserAlreadyExistsException;
import com.reservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthMvcController {
    
    private final AuthService authService;
    
    public AuthMvcController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model
    ) {
        model.addAttribute("pageTitle", "Connexion");
        
        if (error != null) {
            model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté avec succès");
        }
        
        return "auth/login";
    }
    
    /**
     * Page d'inscription
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", "Inscription");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }
    
    /**
     * Traitement de l'inscription
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        
        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Inscription");
            return "auth/register";
        }
        
        try {
            User user = authService.register(registerRequest);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
            
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("pageTitle", "Inscription");
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Inscription");
            model.addAttribute("errorMessage", "Erreur lors de l'inscription. Veuillez réessayer.");
            return "auth/register";
        }
    }
}