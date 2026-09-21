package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class SurveyDTO {
    private String id;
    private String user;
    private String type;

    // Datos demograficos
    private Integer age;
    private String grade;
    private String gender;

    // Respuestas
    private Map<Integer, Integer> answers;
    private Integer totalScore;
    private Map<String, Double> dimensionScores;

    // Metadata
    private Instant createdAt;
}
