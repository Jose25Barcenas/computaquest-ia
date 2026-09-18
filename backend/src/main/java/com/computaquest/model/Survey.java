package com.computaquest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "surveys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {

    @Id
    private String id;

    private String user;

    private String type;

    // Datos demograficos
    private Integer age;

    private String grade;

    private String gender;

    // Respuestas Likert (1-5)
    private Map<Integer, Integer> answers;

    // Puntuacion total
    private Integer totalScore;

    // Puntuaciones por dimension
    private Map<String, Double> dimensionScores;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
