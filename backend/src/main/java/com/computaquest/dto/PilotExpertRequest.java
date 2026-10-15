package com.computaquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class PilotExpertRequest {

    @NotBlank(message = "El codigo del docente es requerido")
    private String reviewerCode;

    @NotNull(message = "Los criterios son requeridos")
    private Map<String, Integer> scores;

    @Size(max = 1000, message = "El comentario es demasiado largo")
    private String comments;

    @NotBlank(message = "El veredicto es requerido")
    private String verdict;
}
