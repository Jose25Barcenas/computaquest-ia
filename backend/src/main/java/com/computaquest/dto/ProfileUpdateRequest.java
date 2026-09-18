package com.computaquest.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String name;

    private String avatar;
}
