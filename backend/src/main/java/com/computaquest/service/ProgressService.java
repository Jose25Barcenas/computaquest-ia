package com.computaquest.service;

import com.computaquest.dto.CompleteChallengeRequest;
import com.computaquest.dto.LeaderboardEntry;
import com.computaquest.dto.ProgressDTO;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Challenge;
import com.computaquest.model.Progress;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    private static final int XP_PER_LEVEL = 500;
    private static final int XP_CHALLENGE_PASS = 100;
    private static final int XP_CHALLENGE_FAIL = 50;
    private static final int POINTS_CHALLENGE_PASS = 10;
    private static final int POINTS_CHALLENGE_FAIL = 5;

    public List<ProgressDTO> getUserProgress(String userId) {
        List<Progress> progressList = progressRepository.findByUser(userId);
        return progressList.stream().map(this::toDTO).toList();
    }

    public ProgressDTO completeChallenge(String userEmail, CompleteChallengeRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));

        Progress progress = progressRepository.findByUserAndChallenge(user.getId(), request.getChallengeId())
                .orElse(Progress.builder()
                        .user(user.getId())
                        .challenge(request.getChallengeId())
                        .build());

        if (progress.getCompleted()) {
            throw new ValidationAppException("Ya completaste este reto anteriormente");
        }

        progress.setScore(request.getScore());
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setLastAttemptAt(Instant.now());

        boolean passed = request.getScore() >= 70;

        if (passed) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());

            user.setXp(user.getXp() + XP_CHALLENGE_PASS);
            user.setPoints(user.getPoints() + POINTS_CHALLENGE_PASS);

            if (challenge.getBadgeName() != null && !user.getBadges().contains(challenge.getBadgeName())) {
                user.setBadges(new ArrayList<>(user.getBadges()));
                user.getBadges().add(challenge.getBadgeName());
            }
        } else {
            user.setXp(user.getXp() + XP_CHALLENGE_FAIL);
            user.setPoints(user.getPoints() + POINTS_CHALLENGE_FAIL);
        }

        while (user.getXp() >= XP_PER_LEVEL) {
            user.setXp(user.getXp() - XP_PER_LEVEL);
            user.setLevel(user.getLevel() + 1);
        }

        userRepository.save(user);
        progress = progressRepository.save(progress);

        return toDTO(progress);
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<User> topUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.computaquest.enums.Role.STUDENT)
                .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
                .limit(10)
                .toList();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            leaderboard.add(LeaderboardEntry.builder()
                    .position(i + 1)
                    .userId(user.getId())
                    .name(user.getName())
                    .avatar(user.getAvatar())
                    .points(user.getPoints())
                    .level(user.getLevel())
                    .xp(user.getXp())
                    .build());
        }
        return leaderboard;
    }

    private ProgressDTO toDTO(Progress progress) {
        Challenge challenge = challengeRepository.findById(progress.getChallenge()).orElse(null);

        return ProgressDTO.builder()
                .id(progress.getId())
                .challengeId(progress.getChallenge())
                .challengeTitle(challenge != null ? challenge.getTitle() : null)
                .challengeType(challenge != null ? challenge.getType() : null)
                .challengeDifficulty(challenge != null ? challenge.getDifficulty() : null)
                .completed(progress.getCompleted())
                .score(progress.getScore())
                .attempts(progress.getAttempts())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}
