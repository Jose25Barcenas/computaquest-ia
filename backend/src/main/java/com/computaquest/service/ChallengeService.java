package com.computaquest.service;

import com.computaquest.dto.ChallengeCreateRequest;
import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.model.Challenge;
import com.computaquest.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<ChallengeDTO> getAllChallenges(ChallengeType type, Integer difficulty) {
        List<Challenge> challenges;

        if (type != null) {
            challenges = challengeRepository.findByTypeAndIsActiveTrue(type);
        } else {
            challenges = challengeRepository.findByIsActiveTrueOrderByOrderAsc();
        }

        if (difficulty != null) {
            challenges = challenges.stream()
                    .filter(c -> c.getDifficulty().equals(difficulty))
                    .toList();
        }

        return challenges.stream().map(this::toDTO).toList();
    }

    public ChallengeDTO getChallengeById(String id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));
        return toDTO(challenge);
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
