package com.computaquest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class PilotTestRequest {

    @NotBlank(message = "El codigo del participante es requerido")
    private String code;

    @NotBlank(message = "El tipo es requerido")
    @Pattern(regexp = "pre|post", message = "El tipo debe ser 'pre' o 'post'")
    private String type;

    @NotNull(message = "Las respuestas son requeridas")
    @Valid
    private Map<Integer, Integer> answers;

    private Instant startedAt;

    @Size(max = 100, message = "La cuenta pseudonima es demasiado larga")
    private String accountEmail;
}
