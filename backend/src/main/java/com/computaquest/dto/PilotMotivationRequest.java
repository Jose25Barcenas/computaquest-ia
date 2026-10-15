package com.computaquest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class PilotMotivationRequest {

    @NotBlank(message = "El codigo del participante es requerido")
    private String code;

    @NotNull(message = "Las respuestas de motivacion son requeridas")
    @Valid
    private Map<Integer, Integer> answers;

    @Size(max = 500, message = "El comentario es demasiado largo")
    private String openAnswer;
}
