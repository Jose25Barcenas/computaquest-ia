package com.computaquest.service;

import com.computaquest.dto.SurveyComparisonDTO;
import com.computaquest.dto.SurveyDTO;
import com.computaquest.dto.SurveyRequest;
import com.computaquest.exception.ConflictException;
import com.computaquest.model.Survey;
import com.computaquest.repository.SurveyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private SurveyRequest surveyRequest(String type) {
        SurveyRequest request = new SurveyRequest();
        request.setType(type);
        request.setAge(13);
        request.setGrade("8vo");
        request.setGender("M");
        Map<Integer, Integer> answers = new HashMap<>();
        for (int i = 1; i <= 15; i++) answers.put(i, 4);
        request.setAnswers(answers);
        return request;
    }

    @Test
    void submitRejectsDuplicateSurveyOfSameType() {
        when(surveyRepository.findFirstByUserAndTypeOrderByCreatedAtDesc("a@test.com", "pre"))
                .thenReturn(Optional.of(survey("a@test.com", "pre", 40, Instant.now())));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> surveyService.submitSurvey("a@test.com", surveyRequest("pre")));

        assertTrue(ex.getMessage().contains("Ya enviaste la encuesta pre"));
    }

    @Test
    void submitAllowsFirstSurveyOfEachType() {
        Survey savedPost = survey("a@test.com", "post", 50, Instant.now());

        when(surveyRepository.findFirstByUserAndTypeOrderByCreatedAtDesc("a@test.com", "post"))
                .thenReturn(Optional.empty());
        when(surveyRepository.save(any(Survey.class))).thenReturn(savedPost);

        SurveyDTO result = surveyService.submitSurvey("a@test.com", surveyRequest("post"));

        assertEquals("post", result.getType());
        assertEquals(50, result.getTotalScore());
    }
}
