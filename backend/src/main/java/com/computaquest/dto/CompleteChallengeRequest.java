package com.computaquest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CompleteChallengeRequest {

    @NotBlank(message = "El ID del reto es requerido")
    private String challengeId;

    // El servidor recalcula el score con la respuesta correcta; el valor del cliente se ignora
    private Integer score;

    private List<String> userAnswers;
}
