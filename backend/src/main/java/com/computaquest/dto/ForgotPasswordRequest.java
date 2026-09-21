package com.computaquest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email no valido")
    private String email;
}
