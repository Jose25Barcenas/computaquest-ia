package com.computaquest.service;

import com.computaquest.dto.CompleteChallengeRequest;
import com.computaquest.dto.ProgressDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Challenge;
import com.computaquest.model.Progress;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ProgressRepository progressRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressService progressService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("u1")
                .name("Estudiante")
                .email("estudiante@test.com")
                .xp(0)
                .points(0)
                .level(1)
                .build();
    }

    private Challenge multipleSelectChallenge() {
        Map<String, Object> content = new HashMap<>();
        content.put("type", "multiple-select");
        content.put("options", List.of("A", "B", "C", "D"));
        content.put("correctAnswers", List.of("A", "B"));
        return Challenge.builder()
                .id("c1")
                .title("Multiple select")
                .type(ChallengeType.ABSTRACTION)
                .content(content)
                .xpReward(100)
                .pointsReward(10)
                .isActive(true)
                .build();
    }

    private CompleteChallengeRequest requestWith(List<String> answers) {
        CompleteChallengeRequest request = new CompleteChallengeRequest();
        request.setChallengeId("c1");
        request.setUserAnswers(answers);
        return request;
    }

    @Test
    void multipleSelectSelectingEverythingDoesNotPass() {
        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(multipleSelectChallenge()));
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressDTO result = progressService.completeChallenge(
                "estudiante@test.com", requestWith(List.of("A", "B", "C", "D")));

        // Marcar todo ya NO aprueba: las selecciones extras penalizan el score
        assertFalse(result.getCompleted());
        assertEquals(0, result.getScore());
        // Solo el consuelo de primer intento (cubierto aparte en failedAttemptGivesConsolationXpOnlyOnce)
        assertEquals(50, result.getXpEarned());
    }

    @Test
    void multipleSelectExactAnswerPasses() {
        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(multipleSelectChallenge()));
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressDTO result = progressService.completeChallenge(
                "estudiante@test.com", requestWith(List.of("A", "B")));

        assertTrue(result.getCompleted());
        assertEquals(100, result.getScore());
        assertEquals(100, result.getXpEarned());
        assertEquals(100, user.getXp());
    }

    @Test
    void failedAttemptGivesConsolationXpOnlyOnce() {
        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(multipleSelectChallenge()));
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Primer intento fallido: si hay consuelo, es una unica vez
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.empty());
        ProgressDTO first = progressService.completeChallenge(
                "estudiante@test.com", requestWith(List.of("C")));
        assertEquals(50, first.getXpEarned());
        assertEquals(50, user.getXp());

        // Segundo intento fallido: sin recompensa (esto es lo que evita el farmeo)
        Progress previous = Progress.builder()
                .user("u1")
                .challenge("c1")
                .attempts(1)
                .score(first.getScore())
                .build();
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.of(previous));

        ProgressDTO second = progressService.completeChallenge(
                "estudiante@test.com", requestWith(List.of("C")));
        assertEquals(0, second.getXpEarned());
        assertEquals(50, user.getXp());
        assertEquals(2, second.getAttempts());
    }

    @Test
    void inactiveChallengeCannotBeCompleted() {
        Challenge inactive = multipleSelectChallenge();
        inactive.setIsActive(false);

        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(inactive));

        assertThrows(ValidationAppException.class,
                () -> progressService.completeChallenge("estudiante@test.com", requestWith(List.of("A"))));
    }

    @Test
    void levelUpIsReportedInResponse() {
        user.setXp(450);
        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(multipleSelectChallenge()));
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressDTO result = progressService.completeChallenge(
                "estudiante@test.com", requestWith(List.of("A", "B")));

        assertTrue(result.getLevelUp());
        assertEquals(2, result.getUserLevel());
        assertEquals(50, result.getUserXp());
    }

    @Test
    void completedChallengeCannotBeRepeated() {
        Progress done = Progress.builder()
                .user("u1")
                .challenge("c1")
                .completed(true)
                .attempts(1)
                .build();

        when(userRepository.findByEmail("estudiante@test.com")).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(multipleSelectChallenge()));
        when(progressRepository.findByUserAndChallenge("u1", "c1")).thenReturn(Optional.of(done));

        assertThrows(ValidationAppException.class,
                () -> progressService.completeChallenge("estudiante@test.com", requestWith(List.of("A"))));
    }
}
