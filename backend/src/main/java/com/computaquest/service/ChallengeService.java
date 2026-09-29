package com.computaquest.service;

import com.computaquest.dto.ChallengeCreateRequest;
import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Challenge;
import com.computaquest.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<ChallengeDTO> getAllChallenges(ChallengeType type, Integer difficulty) {
        List<Challenge> challenges;

        if (type != null) {
            challenges = challengeRepository.findByTypeAndIsActiveTrueOrderByOrderAsc(type);
        } else {
            challenges = challengeRepository.findByIsActiveTrueOrderByOrderAsc();
        }

        if (difficulty != null) {
            challenges = challenges.stream()
                    .filter(c -> c.getDifficulty().equals(difficulty))
                    .toList();
        }

        return challenges.stream().map(this::toDTOSafe).toList();
    }

    public ChallengeDTO getChallengeById(String id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));
        if (!Boolean.TRUE.equals(challenge.getIsActive())) {
            throw new ResourceNotFoundException("Reto no encontrado");
        }
        return toDTOSafe(challenge);
    }

    public ChallengeDTO createChallenge(ChallengeCreateRequest request) {
        validateContent(request.getType(), request.getContent());
        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : 1)
                .xpReward(request.getXpReward() != null ? request.getXpReward() : 100)
                .pointsReward(request.getPointsReward() != null ? request.getPointsReward() : 10)
                .badgeName(request.getBadgeName())
                .content(request.getContent())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .order(request.getOrder() != null ? request.getOrder() : 0)
                .build();

        challenge = challengeRepository.save(challenge);
        return toDTO(challenge);
    }

    public ChallengeDTO updateChallenge(String id, ChallengeCreateRequest request) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));

        if (request.getContent() != null) {
            validateContent(request.getType() != null ? request.getType() : challenge.getType(), request.getContent());
        }

        if (request.getTitle() != null) challenge.setTitle(request.getTitle());
        if (request.getDescription() != null) challenge.setDescription(request.getDescription());
        if (request.getType() != null) challenge.setType(request.getType());
        if (request.getDifficulty() != null) challenge.setDifficulty(request.getDifficulty());
        if (request.getXpReward() != null) challenge.setXpReward(request.getXpReward());
        if (request.getPointsReward() != null) challenge.setPointsReward(request.getPointsReward());
        if (request.getBadgeName() != null) challenge.setBadgeName(request.getBadgeName());
        if (request.getContent() != null) challenge.setContent(request.getContent());
        if (request.getOrder() != null) challenge.setOrder(request.getOrder());

        challenge = challengeRepository.save(challenge);
        return toDTO(challenge);
    }

    public void deleteChallenge(String id) {
        if (!challengeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reto no encontrado");
        }
        challengeRepository.deleteById(id);
    }

    private static final Set<String> SUPPORTED_FORMATS = Set.of("drag-drop", "quiz", "multiple-select");

    @SuppressWarnings("unchecked")
    private void validateContent(ChallengeType type, Map<String, Object> content) {
        if (content == null || content.isEmpty()) {
            throw new ValidationAppException("El contenido del reto es requerido");
        }

        Object rawFormat = content.get("type");
        if (!(rawFormat instanceof String format) || !SUPPORTED_FORMATS.contains(format)) {
            throw new ValidationAppException(
                    "content.type debe ser uno de: drag-drop, quiz, multiple-select");
        }

        switch (format) {
            case "drag-drop" -> {
                List<?> items = asList(content.get("items"));
                List<?> order = asList(content.get("correctOrder"));
                if (items == null || items.isEmpty()) {
                    throw new ValidationAppException("drag-drop: 'items' debe ser una lista no vacia");
                }
                if (order == null || order.isEmpty()) {
                    throw new ValidationAppException("drag-drop: 'correctOrder' debe ser una lista no vacia");
                }
                if (order.size() != items.size()) {
                    throw new ValidationAppException("drag-drop: 'correctOrder' debe incluir todos los items");
                }
            }
            case "quiz" -> {
                List<?> questions = asList(content.get("questions"));
                if (questions == null || questions.isEmpty()) {
                    throw new ValidationAppException("quiz: 'questions' debe ser una lista no vacia");
                }
                for (int i = 0; i < questions.size(); i++) {
                    String where = "quiz: pregunta " + (i + 1);
                    if (!(questions.get(i) instanceof Map<?, ?> q)) {
                        throw new ValidationAppException(where + " debe ser un objeto");
                    }
                    if (!(q.get("q") instanceof String qText) || qText.isBlank()) {
                        throw new ValidationAppException(where + " necesita el campo 'q'");
                    }
                    List<?> opts = asList(q.get("opts"));
                    if (opts == null || opts.isEmpty()) {
                        throw new ValidationAppException(where + " necesita 'opts' no vacio");
                    }
                    if (!q.containsKey("a")) {
                        throw new ValidationAppException(where + " necesita la respuesta 'a'");
                    }
                    if (!opts.contains(q.get("a"))) {
                        throw new ValidationAppException(where + ": la respuesta 'a' debe estar dentro de 'opts'");
                    }
                }
            }
            case "multiple-select" -> {
                List<?> options = asList(content.get("options"));
                List<?> correct = asList(content.get("correctAnswers"));
                if (options == null || options.isEmpty()) {
                    throw new ValidationAppException("multiple-select: 'options' debe ser una lista no vacia");
                }
                if (correct == null || correct.isEmpty()) {
                    throw new ValidationAppException("multiple-select: 'correctAnswers' debe ser una lista no vacia");
                }
                for (Object c : correct) {
                    if (!options.contains(c)) {
                        throw new ValidationAppException(
                                "multiple-select: cada 'correctAnswers' debe existir en 'options'");
                    }
                }
            }
            default -> throw new ValidationAppException("Formato de reto no soportado");
        }
    }

    private List<?> asList(Object value) {
        return value instanceof List<?> list ? list : null;
    }

    @SuppressWarnings("unchecked")
    private ChallengeDTO toDTOSafe(Challenge challenge) {
        Map<String, Object> safeContent = challenge.getContent() != null
                ? new HashMap<>(challenge.getContent())
                : null;

        if (safeContent != null) {
            safeContent.remove("correctOrder");
            safeContent.remove("correctAnswers");

            Object questions = safeContent.get("questions");
            if (questions instanceof List<?> qList) {
                List<Map<String, Object>> safeQuestions = new ArrayList<>();
                for (Object q : qList) {
                    if (q instanceof Map<?, ?> qMap) {
                        Map<String, Object> sq = new HashMap<>();
                        qMap.forEach((k, v) -> {
                            if (!"a".equals(k)) sq.put((String) k, v);
                        });
                        safeQuestions.add(sq);
                    }
                }
                safeContent.put("questions", safeQuestions);
            }
        }

        return ChallengeDTO.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .difficulty(challenge.getDifficulty())
                .xpReward(challenge.getXpReward())
                .pointsReward(challenge.getPointsReward())
                .badgeName(challenge.getBadgeName())
                .content(safeContent)
                .isActive(challenge.getIsActive())
                .order(challenge.getOrder())
                .build();
    }

    private ChallengeDTO toDTO(Challenge challenge) {
        return ChallengeDTO.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .difficulty(challenge.getDifficulty())
                .xpReward(challenge.getXpReward())
                .pointsReward(challenge.getPointsReward())
                .badgeName(challenge.getBadgeName())
                .content(challenge.getContent())
                .isActive(challenge.getIsActive())
                .order(challenge.getOrder())
                .build();
    }
}
