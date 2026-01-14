package com.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public endpoint - Accessible sans authentification";
    }
    
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "Protected endpoint - Vous êtes authentifié !";
    }
}