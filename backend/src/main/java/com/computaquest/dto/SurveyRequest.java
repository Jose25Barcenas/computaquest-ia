package com.computaquest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SurveyRequest {

    @NotNull(message = "El tipo de encuesta es requerido")
    private String type;

    // Datos demograficos
    @NotNull(message = "La edad es requerida")
    @Min(value = 10, message = "La edad minima es 10 anos")
    @Max(value = 15, message = "La edad maxima es 15 anos")
    private Integer age;

    @NotBlank(message = "El grado escolar es requerido")
    private String grade;

    @NotBlank(message = "El genero es requerido")
    private String gender;

    // Respuestas Likert (1-5)
    @NotNull(message = "Las respuestas son requeridas")
    private Map<Integer, Integer> answers;
}
