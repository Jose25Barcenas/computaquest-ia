package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyComparisonDTO {
    private int preCount;
    private int postCount;
    private int pairedCount;
    private double preAverage;
    private double postAverage;
    private double averageDelta;
    private int maxScore;
    private Map<String, Double> preDimensions;
    private Map<String, Double> postDimensions;
    private Map<String, Double> dimensionDelta;
    private List<PairedResult> paired;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairedResult {
        private String user;
        private String grade;
        private int pre;
        private int post;
        private int delta;
    }
}
