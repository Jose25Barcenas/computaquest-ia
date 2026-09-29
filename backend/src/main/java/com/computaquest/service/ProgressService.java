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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    private static final int XP_PER_LEVEL = 500;
    private static final int XP_CHALLENGE_FAIL = 50;
    private static final int POINTS_CHALLENGE_FAIL = 5;

    public List<ProgressDTO> getUserProgress(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Progress> progressList = progressRepository.findByUser(user.getId());
        if (progressList.isEmpty()) {
            return List.of();
        }

        List<String> challengeIds = progressList.stream()
                .map(Progress::getChallenge)
                .distinct()
                .toList();

        Map<String, Challenge> challengeMap = challengeRepository.findAllById(challengeIds).stream()
                .collect(Collectors.toMap(Challenge::getId, c -> c));

        return progressList.stream().map(progress -> {
            Challenge challenge = challengeMap.get(progress.getChallenge());
            return ProgressDTO.builder()
                    .id(progress.getId())
                    .challengeId(progress.getChallenge())
                    .challengeTitle(challenge != null ? challenge.getTitle() : null)
                    .challengeType(challenge != null ? challenge.getType() : null)
                    .challengeDifficulty(challenge != null ? challenge.getDifficulty() : null)
                    .completed(progress.isCompleted())
                    .score(progress.getScore())
                    .attempts(progress.getAttempts())
                    .completedAt(progress.getCompletedAt())
                    .build();
        }).toList();
    }

    public synchronized ProgressDTO completeChallenge(String userEmail, CompleteChallengeRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));

        if (!Boolean.TRUE.equals(challenge.getIsActive())) {
            throw new ValidationAppException("Este reto ya no esta disponible");
        }

        Progress progress = progressRepository.findByUserAndChallenge(user.getId(), request.getChallengeId())
                .orElse(Progress.builder()
                        .user(user.getId())
                        .challenge(request.getChallengeId())
                        .build());

        if (progress.isCompleted()) {
            throw new ValidationAppException("Ya completaste este reto anteriormente");
        }

        int attemptsBefore = progress.getAttempts();
        int score = calculateServerScore(challenge, request);
        boolean passed = score >= 70;

        progress.setScore(score);
        progress.setAttempts(attemptsBefore + 1);
        progress.setLastAttemptAt(Instant.now());

        int xpEarned = 0;
        int pointsEarned = 0;

        if (passed) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());

            xpEarned = challenge.getXpReward() > 0 ? challenge.getXpReward() : 100;
            pointsEarned = challenge.getPointsReward() > 0 ? challenge.getPointsReward() : 10;
            user.setXp(user.getXp() + xpEarned);
            user.setPoints(user.getPoints() + pointsEarned);

            if (challenge.getBadgeName() != null && !user.getBadges().contains(challenge.getBadgeName())) {
                user.setBadges(new ArrayList<>(user.getBadges()));
                user.getBadges().add(challenge.getBadgeName());
            }

            log.info("Usuario {} completó reto '{}' con score {} (passed={})", userEmail, challenge.getTitle(), score, passed);
        } else if (attemptsBefore == 0) {
            // Consuelo solo en el primer intento: evita farmear XP repitiendo el reto
            xpEarned = XP_CHALLENGE_FAIL;
            pointsEarned = POINTS_CHALLENGE_FAIL;
            user.setXp(user.getXp() + xpEarned);
            user.setPoints(user.getPoints() + pointsEarned);

            log.info("Usuario {} intentó reto '{}' con score {} (passed={})", userEmail, challenge.getTitle(), score, passed);
        } else {
            log.info("Usuario {} reintentó reto '{}' con score {} (passed={})", userEmail, challenge.getTitle(), score, passed);
        }

        boolean levelUp = false;
        while (user.getXp() >= XP_PER_LEVEL) {
            user.setXp(user.getXp() - XP_PER_LEVEL);
            user.setLevel(user.getLevel() + 1);
            levelUp = true;
            log.info("Usuario {} subió a nivel {}", userEmail, user.getLevel());
        }

        progress = progressRepository.save(progress);
        User savedUser = userRepository.save(user);

        Challenge challengeRef = challenge;
        return ProgressDTO.builder()
                .id(progress.getId())
                .challengeId(progress.getChallenge())
                .challengeTitle(challengeRef.getTitle())
                .challengeType(challengeRef.getType())
                .challengeDifficulty(challengeRef.getDifficulty())
                .completed(progress.isCompleted())
                .score(progress.getScore())
                .attempts(progress.getAttempts())
                .completedAt(progress.getCompletedAt())
                .xpEarned(xpEarned)
                .pointsEarned(pointsEarned)
                .userXp(savedUser.getXp())
                .userPoints(savedUser.getPoints())
                .userLevel(savedUser.getLevel())
                .levelUp(levelUp)
                .build();
    }

    @SuppressWarnings("unchecked")
    private int calculateServerScore(Challenge challenge, CompleteChallengeRequest request) {
        Map<String, Object> content = challenge.getContent();
        if (content == null) return 0;

        String type = (String) content.get("type");
        if (type == null) return 0;

        if (request.getUserAnswers() == null || request.getUserAnswers().isEmpty()) return 0;

        int totalQuestions = 0;
        int correctAnswers = 0;

        switch (type) {
            case "drag-drop" -> {
                List<String> correctOrder = (List<String>) content.get("correctOrder");
                if (correctOrder != null) {
                    totalQuestions = correctOrder.size();
                    List<String> userAnswers = request.getUserAnswers();
                    for (int i = 0; i < Math.min(userAnswers.size(), correctOrder.size()); i++) {
                        if (correctOrder.get(i).equals(userAnswers.get(i))) {
                            correctAnswers++;
                        }
                    }
                }
            }
            case "quiz" -> {
                List<Map<String, Object>> questions = (List<Map<String, Object>>) content.get("questions");
                if (questions != null) {
                    totalQuestions = questions.size();
                    List<String> userAnswers = request.getUserAnswers();
                    for (int i = 0; i < Math.min(userAnswers.size(), questions.size()); i++) {
                        String correct = (String) questions.get(i).get("a");
                        if (correct != null && correct.equals(userAnswers.get(i))) {
                            correctAnswers++;
                        }
                    }
                }
            }
            case "multiple-select" -> {
                List<String> correctAnswersList = (List<String>) content.get("correctAnswers");
                if (correctAnswersList != null) {
                    totalQuestions = correctAnswersList.size();
                    List<String> userAnswers = request.getUserAnswers();
                    for (String correct : correctAnswersList) {
                        if (userAnswers.contains(correct)) {
                            correctAnswers++;
                        }
                    }
                    // Penalizar selecciones extra: sin esto, marcar todo da 100%
                    long extras = userAnswers.stream().filter(a -> !correctAnswersList.contains(a)).count();
                    correctAnswers -= extras;
                }
            }
            default -> {
                return 0;
            }
        }

        if (totalQuestions == 0) return 0;
        int raw = (int) Math.round((double) correctAnswers / totalQuestions * 100);
        return Math.max(0, raw);
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<User> topUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "points"))
                .stream()
                .filter(u -> u.getRole() == com.computaquest.enums.Role.STUDENT)
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
}
