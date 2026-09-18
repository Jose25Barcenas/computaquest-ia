package com.computaquest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteChallengeRequest {

    @NotBlank(message = "El ID del reto es requerido")
    private String challengeId;

    @NotNull(message = "El score es requerido")
    @Min(value = 0, message = "El score no puede ser negativo")
    private Integer score;
}
