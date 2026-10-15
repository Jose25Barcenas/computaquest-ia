package com.computaquest.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Document(collection = "pilot_sessions")
public class PilotSession {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String accountEmail;

    private TestResult pretest;
    private TestResult postest;

    private Map<Integer, Integer> motivationAnswers = new LinkedHashMap<>();
    private String openAnswer;
    private Instant motivationSubmittedAt;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @Data
    public static class TestResult {
        private String type;
        private Map<Integer, Integer> answers = new LinkedHashMap<>();
        private int totalScore;
        private Map<String, Integer> pillarScores = new LinkedHashMap<>();
        private Instant startedAt;
        private Instant submittedAt;
        private Long durationSeconds;
    }
}
