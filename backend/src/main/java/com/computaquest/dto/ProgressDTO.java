package com.computaquest.dto;

import com.computaquest.enums.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class ProgressDTO {
    private String id;
    private String challengeId;
    private String challengeTitle;
    private ChallengeType challengeType;
    private Integer challengeDifficulty;
    private Boolean completed;
    private Integer score;
    private Integer attempts;
    private Instant completedAt;
}
