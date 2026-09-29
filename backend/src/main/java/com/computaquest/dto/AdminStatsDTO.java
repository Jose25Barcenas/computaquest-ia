package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long studentsTotal;
    private long studentsActive7d;
    private long challengesTotal;
    private long challengesActive;
    private long challengesCompleted;
    private double completionRate;
    private long surveysPre;
    private long surveysPost;
}
