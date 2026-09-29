package com.computaquest.service;

import com.computaquest.dto.ChallengeCreateRequest;
import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Challenge;
import com.computaquest.repository.ChallengeRepository;
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
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeService challengeService;

    private Challenge createSampleChallenge() {
        return Challenge.builder()
                .id("1")
                .title("Test Challenge")
                .description("A test challenge")
                .type(ChallengeType.DECOMPOSITION)
                .difficulty(1)
                .xpReward(100)
                .pointsReward(10)
                .isActive(true)
                .order(1)
                .build();
    }

    @Test
    void getAllChallengesReturnsList() {
        when(challengeRepository.findByIsActiveTrueOrderByOrderAsc())
                .thenReturn(List.of(createSampleChallenge()));

        List<ChallengeDTO> result = challengeService.getAllChallenges(null, null);

        assertFalse(result.isEmpty());
        assertEquals("Test Challenge", result.get(0).getTitle());
    }

    @Test
    void getAllChallengesByType() {
        when(challengeRepository.findByTypeAndIsActiveTrueOrderByOrderAsc(ChallengeType.DECOMPOSITION))
                .thenReturn(List.of(createSampleChallenge()));

        List<ChallengeDTO> result = challengeService.getAllChallenges(ChallengeType.DECOMPOSITION, null);

        assertEquals(1, result.size());
    }

    @Test
    void getChallengeByIdFound() {
        when(challengeRepository.findById("1")).thenReturn(Optional.of(createSampleChallenge()));

        ChallengeDTO result = challengeService.getChallengeById("1");

        assertEquals("Test Challenge", result.getTitle());
    }

    @Test
    void getChallengeByIdNotFoundThrows() {
        when(challengeRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> challengeService.getChallengeById("999"));
    }

    @Test
    void createChallengeReturnsDTO() {
        ChallengeCreateRequest request = new ChallengeCreateRequest();
        request.setTitle("New Challenge");
        request.setType(ChallengeType.ALGORITHMS);
        request.setContent(validQuizContent());

        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> {
            Challenge c = inv.getArgument(0);
            c.setId("2");
            return c;
        });

        ChallengeDTO result = challengeService.createChallenge(request);

        assertEquals("New Challenge", result.getTitle());
        assertEquals(ChallengeType.ALGORITHMS, result.getType());
    }

    @Test
    void createChallengeWithUnknownFormatRejected() {
        ChallengeCreateRequest request = new ChallengeCreateRequest();
        request.setTitle("Bad");
        request.setType(ChallengeType.ALGORITHMS);
        request.setContent(Map.of("type", "vero-que-no-existe"));

        assertThrows(ValidationAppException.class, () -> challengeService.createChallenge(request));
        verify(challengeRepository, never()).save(any(Challenge.class));
    }

    @Test
    void createChallengeQuizWithoutAnswerRejected() {
        ChallengeCreateRequest request = new ChallengeCreateRequest();
        request.setTitle("Bad quiz");
        request.setType(ChallengeType.PATTERNS);
        Map<String, Object> question = new HashMap<>();
        question.put("q", "Pregunta sin respuesta");
        question.put("opts", List.of("a", "b"));
        request.setContent(Map.of("type", "quiz", "questions", List.of(question)));

        assertThrows(ValidationAppException.class, () -> challengeService.createChallenge(request));
    }

    @Test
    void createChallengeEmptyQuestionsRejected() {
        ChallengeCreateRequest request = new ChallengeCreateRequest();
        request.setTitle("Empty quiz");
        request.setType(ChallengeType.PATTERNS);
        request.setContent(Map.of("type", "quiz", "questions", List.of()));

        assertThrows(ValidationAppException.class, () -> challengeService.createChallenge(request));
    }

    @Test
    void createChallengeMultipleSelectWithUnknownAnswerRejected() {
        ChallengeCreateRequest request = new ChallengeCreateRequest();
        request.setTitle("Bad select");
        request.setType(ChallengeType.ABSTRACTION);
        request.setContent(Map.of(
                "type", "multiple-select",
                "options", List.of("op1", "op2"),
                "correctAnswers", List.of("op1", "que-no-existe")));

        assertThrows(ValidationAppException.class, () -> challengeService.createChallenge(request));
    }

    @Test
    void getChallengeByIdInactiveNotFound() {
        Challenge inactive = createSampleChallenge();
        inactive.setIsActive(false);
        when(challengeRepository.findById("1")).thenReturn(Optional.of(inactive));

        assertThrows(ResourceNotFoundException.class, () -> challengeService.getChallengeById("1"));
    }

    private Map<String, Object> validQuizContent() {
        Map<String, Object> question = new HashMap<>();
        question.put("q", "¿Cuanto es 2+2?");
        question.put("opts", List.of("3", "4"));
        question.put("a", "4");
        Map<String, Object> content = new HashMap<>();
        content.put("type", "quiz");
        content.put("questions", List.of(question));
        return content;
    }

    @Test
    void deleteChallengeExisting() {
        when(challengeRepository.existsById("1")).thenReturn(true);
        doNothing().when(challengeRepository).deleteById("1");

        assertDoesNotThrow(() -> challengeService.deleteChallenge("1"));
    }

    @Test
    void deleteChallengeNotFoundThrows() {
        when(challengeRepository.existsById("999")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> challengeService.deleteChallenge("999"));
    }
}
