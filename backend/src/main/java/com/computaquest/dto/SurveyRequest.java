package com.computaquest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class SurveyRequest {

    @NotNull(message = "El tipo de encuesta es requerido")
    @Pattern(regexp = "pre|post", message = "El tipo debe ser 'pre' o 'post'")
    private String type;

    // Datos demograficos
    @NotNull(message = "La edad es requerida")
    @Min(value = 10, message = "La edad minima es 10 anos")
    @Max(value = 15, message = "La edad maxima es 15 anos")
    private Integer age;

    @NotBlank(message = "El grado escolar es requerido")
    @Pattern(regexp = "8vo|9no", message = "El grado debe ser '8vo' o '9no'")
    private String grade;

    @NotBlank(message = "El genero es requerido")
    @Pattern(regexp = "M|F|O", message = "El genero debe ser M, F o O")
    private String gender;

    // Respuestas Likert (1-5)
    @NotNull(message = "Las respuestas son requeridas")
    @Valid
    private Map<Integer, Integer> answers;
}
