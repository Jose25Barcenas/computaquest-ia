package com.computaquest.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Document(collection = "pilot_expert_reviews")
public class PilotExpertReview {

    @Id
    private String id;

    private String reviewerCode;

    private Map<String, Integer> scores = new LinkedHashMap<>();

    private String comments;

    private String verdict;

    private Instant createdAt = Instant.now();
}
