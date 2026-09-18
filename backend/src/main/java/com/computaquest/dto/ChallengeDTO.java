package com.computaquest.dto;

import com.computaquest.enums.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ChallengeDTO {
    private String id;
    private String title;
    private String description;
    private ChallengeType type;
    private Integer difficulty;
    private Integer xpReward;
    private Integer pointsReward;
    private String badgeName;
    private Map<String, Object> content;
    private Boolean isActive;
    private Integer order;
}
