package com.computaquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "La contrasena actual es requerida")
    private String currentPassword;

    @NotBlank(message = "La contrasena nueva es requerida")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String newPassword;
}
