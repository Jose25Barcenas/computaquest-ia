package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeStatsDTO {
    private String challengeId;
    private String title;
    private String type;
    private long uniqueCompletions;
    private long attempts;
    private double avgScore;
    private double completionRate;
}
