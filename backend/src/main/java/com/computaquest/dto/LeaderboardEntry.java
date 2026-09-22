package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LeaderboardEntry {
    private Integer position;
    private String name;
    private String avatar;
    private Integer points;
    private Integer level;
    private Integer xp;
}
