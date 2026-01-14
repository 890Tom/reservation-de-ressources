package com.reservation.dto.request;

import com.reservation.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Email(message = "L'email doit être valide")
    private String email;
    
    private String firstName;
    private String lastName;
    private String department;
    private Role role;
    private Boolean active;
}