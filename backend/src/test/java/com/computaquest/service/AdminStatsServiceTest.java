package com.computaquest.service;

import com.computaquest.dto.ActivityDTO;
import com.computaquest.dto.ChallengeStatsDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.enums.Role;
import com.computaquest.model.Challenge;
import com.computaquest.model.Progress;
import com.computaquest.model.Survey;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.SurveyRepository;
import com.computaquest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ProgressRepository progressRepository;
    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    private Challenge challenge(String id) {
        return Challenge.builder()
                .id(id)
                .title("Reto " + id)
                .type(ChallengeType.DECOMPOSITION)
                .order(1)
                .build();
    }

    private Progress progress(String challengeId, boolean completed, int score, int attempts, Instant at) {
        return Progress.builder()
                .user("u1")
                .challenge(challengeId)
                .completed(completed)
                .score(score)
                .attempts(attempts)
                .lastAttemptAt(at)
                .completedAt(completed ? at : null)
                .build();
    }

    @Test
    void challengeStatsCalculaCompletadosIntentosYTasa() {
        Instant now = Instant.now();
        when(challengeRepository.findAll()).thenReturn(List.of(challenge("c1")));
        when(progressRepository.findAll()).thenReturn(List.of(
                progress("c1", true, 100, 2, now),
                progress("c1", false, 0, 3, now)));
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(2L);

        List<ChallengeStatsDTO> stats = adminStatsService.getChallengeStats();

        assertEquals(1, stats.size());
        ChallengeStatsDTO s = stats.get(0);
        assertEquals("c1", s.getChallengeId());
        assertEquals(1, s.getUniqueCompletions());
        assertEquals(5, s.getAttempts());
        assertEquals(100.0, s.getAvgScore());
        assertEquals(50.0, s.getCompletionRate());
    }

    @Test
    void challengeStatsSinEstudiantesNoDividePorCero() {
        when(challengeRepository.findAll()).thenReturn(List.of(challenge("c1")));
        when(progressRepository.findAll()).thenReturn(List.of());
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(0L);

        List<ChallengeStatsDTO> stats = adminStatsService.getChallengeStats();

        assertEquals(1, stats.size());
        assertEquals(0.0, stats.get(0).getCompletionRate());
        assertEquals(0, stats.get(0).getUniqueCompletions());
    }

    @Test
    void recentActivityOrdenaPorFechaYLimita() {
        Instant old = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant recent = Instant.now();

        User user = User.builder().id("u1").name("Maria").email("maria@test.com").build();
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(challengeRepository.findAll()).thenReturn(List.of(challenge("c1")));
        when(progressRepository.findAll()).thenReturn(List.of(progress("c1", true, 80, 1, old)));
        when(surveyRepository.findAll()).thenReturn(List.of(Survey.builder()
                .user("u1")
                .type("post")
                .totalScore(45)
                .createdAt(recent)
                .build()));

        List<ActivityDTO> activity = adminStatsService.getRecentActivity(5);

        assertEquals(2, activity.size());
        assertEquals("SURVEY", activity.get(0).getType());
        assertEquals("Maria", activity.get(0).getUser());
        assertTrue(activity.get(0).getDetail().contains("post"));
        assertEquals("CHALLENGE", activity.get(1).getType());
        assertTrue(activity.get(1).getDetail().contains("Reto c1"));

        List<ActivityDTO> limited = adminStatsService.getRecentActivity(1);
        assertEquals(1, limited.size());
        assertEquals("SURVEY", limited.get(0).getType());
    }

    @Test
    void recentActivitySinDatosDevuelveVacio() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(progressRepository.findAll()).thenReturn(List.of());
        when(surveyRepository.findAll()).thenReturn(List.of());

        assertTrue(adminStatsService.getRecentActivity(10).isEmpty());
    }

    @Test
    void recentActivityLimitaElMaximoA50() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(challengeRepository.findAll()).thenReturn(List.of(challenge("c1")));
        Instant now = Instant.now();
        List<Progress> many = java.util.stream.IntStream.range(0, 60)
                .mapToObj(i -> progress("c1", true, 100, 1, now.minus(i, ChronoUnit.MINUTES)))
                .toList();
        when(progressRepository.findAll()).thenReturn(many);
        when(surveyRepository.findAll()).thenReturn(List.of());

        List<ActivityDTO> activity = adminStatsService.getRecentActivity(999);

        assertEquals(50, activity.size());
        assertEquals("CHALLENGE", activity.get(0).getType());
    }
}
