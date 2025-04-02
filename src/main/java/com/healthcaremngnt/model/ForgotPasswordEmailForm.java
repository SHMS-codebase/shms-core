package com.healthcaremngnt.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordEmailForm {

	@NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
}