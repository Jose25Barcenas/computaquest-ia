package com.computaquest.service;

import com.computaquest.dto.SurveyComparisonDTO;
import com.computaquest.model.Survey;
import com.computaquest.repository.SurveyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Survey survey(String user, String type, int totalScore, Instant createdAt) {
        return Survey.builder()
                .user(user)
                .type(type)
                .totalScore(totalScore)
                .dimensionScores(Map.of("descomposicion", (double) Math.min(5, totalScore / 15)))
                .createdAt(createdAt)
                .build();
    }

    @Test
    void comparisonPairsPreAndPostPerStudent() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-06-01T00:00:00Z");
        when(surveyRepository.findAll()).thenReturn(List.of(
                survey("a@test.com", "pre", 30, t1),
                survey("a@test.com", "post", 55, t2),
                survey("b@test.com", "pre", 40, t1),
                survey("c@test.com", "post", 60, t2) // sin pre: no entra en pareados
        ));

        SurveyComparisonDTO result = surveyService.getComparison();

        assertEquals(2, result.getPreCount());
        assertEquals(2, result.getPostCount());
        assertEquals(1, result.getPairedCount());
        assertEquals(75, result.getMaxScore());

        SurveyComparisonDTO.PairedResult paired = result.getPaired().get(0);
        assertEquals("a@test.com", paired.getUser());
        assertEquals(30, paired.getPre());
        assertEquals(55, paired.getPost());
        assertEquals(25, paired.getDelta());

        assertEquals(35.0, result.getPreAverage());
        assertEquals(57.5, result.getPostAverage());
        assertEquals(22.5, result.getAverageDelta());
    }

    @Test
    void comparisonUsesLatestSurveyWhenThereAreMany() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-06-01T00:00:00Z");
        when(surveyRepository.findAll()).thenReturn(List.of(
                survey("a@test.com", "pre", 20, t1),
                survey("a@test.com", "pre", 45, t2), // la mas reciente gana
                survey("a@test.com", "post", 50, t2)
        ));

        SurveyComparisonDTO result = surveyService.getComparison();

        assertEquals(1, result.getPreCount());
        assertEquals(45, result.getPaired().get(0).getPre());
        assertEquals(5, result.getPaired().get(0).getDelta());
    }

    @Test
    void comparisonWithNoDataReturnsEmptyNotCrash() {
        when(surveyRepository.findAll()).thenReturn(List.of());

        SurveyComparisonDTO result = surveyService.getComparison();

        assertEquals(0, result.getPairedCount());
        assertEquals(0.0, result.getPreAverage());
        assertEquals(0.0, result.getAverageDelta());
        assertTrue(result.getPaired().isEmpty());
    }
}
