package com.computaquest.service;

import com.computaquest.dto.ChallengeCreateRequest;
import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.exception.ResourceNotFoundException;
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
        return toDTOSafe(challenge);
    }

    public ChallengeDTO createChallenge(ChallengeCreateRequest request) {
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
