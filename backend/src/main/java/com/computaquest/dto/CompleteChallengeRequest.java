package com.computaquest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CompleteChallengeRequest {

    @NotBlank(message = "El ID del reto es requerido")
    private String challengeId;

    @NotNull(message = "El score es requerido")
    @Min(value = 0, message = "El score no puede ser negativo")
    @Max(value = 100, message = "El score no puede ser mayor a 100")
    private Integer score;

    private List<String> userAnswers;
}
