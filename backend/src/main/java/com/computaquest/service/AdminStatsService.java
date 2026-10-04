package com.computaquest.service;

import com.computaquest.dto.ActivityDTO;
import com.computaquest.dto.AdminStatsDTO;
import com.computaquest.dto.ChallengeStatsDTO;
import com.computaquest.enums.Role;
import com.computaquest.model.Challenge;
import com.computaquest.model.Progress;
import com.computaquest.model.Survey;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.SurveyRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ProgressRepository progressRepository;
    private final SurveyRepository surveyRepository;

    public AdminStatsDTO getStats() {
        long studentsTotal = userRepository.countByRole(Role.STUDENT);
        long studentsActive7d = userRepository.countByRoleAndLastLoginDateAfter(
                Role.STUDENT, Instant.now().minus(7, ChronoUnit.DAYS));
        long challengesTotal = challengeRepository.count();
        long challengesActive = challengeRepository.countByIsActiveTrue();
        long challengesCompleted = progressRepository.countByCompletedTrue();

        double completionRate = 0.0;
        if (studentsTotal > 0 && challengesActive > 0) {
            double rate = (double) challengesCompleted / (studentsTotal * challengesActive) * 100;
            completionRate = Math.min(100, Math.round(rate * 100.0) / 100.0);
        }

        return AdminStatsDTO.builder()
                .studentsTotal(studentsTotal)
                .studentsActive7d(studentsActive7d)
                .challengesTotal(challengesTotal)
                .challengesActive(challengesActive)
                .challengesCompleted(challengesCompleted)
                .completionRate(completionRate)
                .surveysPre(surveyRepository.countByType("pre"))
                .surveysPost(surveyRepository.countByType("post"))
                .build();
    }

    public List<ChallengeStatsDTO> getChallengeStats() {
        List<Challenge> challenges = challengeRepository.findAll();
        List<Progress> allProgress = progressRepository.findAll();
        long studentsTotal = userRepository.countByRole(Role.STUDENT);

        return challenges.stream()
                .sorted(Comparator.comparing(Challenge::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(challenge -> {
                    List<Progress> progress = allProgress.stream()
                            .filter(p -> challenge.getId().equals(p.getChallenge()))
                            .toList();
                    long completions = progress.stream().filter(Progress::isCompleted).count();
                    long attempts = progress.stream()
                            .mapToLong(p -> p.getAttempts() == null ? 0L : p.getAttempts())
                            .sum();
                    double avgScore = progress.stream()
                            .filter(Progress::isCompleted)
                            .mapToDouble(p -> p.getScore() == null ? 0 : p.getScore())
                            .average()
                            .orElse(0.0);
                    double rate = studentsTotal > 0
                            ? Math.min(100, Math.round(completions * 100.0 / studentsTotal))
                            : 0.0;

                    return ChallengeStatsDTO.builder()
                            .challengeId(challenge.getId())
                            .title(challenge.getTitle())
                            .type(challenge.getType() == null ? "" : challenge.getType().name())
                            .uniqueCompletions(completions)
                            .attempts(attempts)
                            .avgScore(Math.round(avgScore * 10.0) / 10.0)
                            .completionRate(rate)
                            .build();
                })
                .toList();
    }

    public List<ActivityDTO> getRecentActivity(int limit) {
        int size = Math.max(1, Math.min(limit, 50));

        Map<String, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u.getName() != null ? u.getName() : u.getEmail(),
                        (a, b) -> a));
        Map<String, String> challengeTitles = challengeRepository.findAll().stream()
                .collect(Collectors.toMap(Challenge::getId, Challenge::getTitle, (a, b) -> a));

        List<ActivityDTO> events = new ArrayList<>();

        for (Progress p : progressRepository.findAll()) {
            String name = userNames.getOrDefault(p.getUser(), "Usuario");
            String title = challengeTitles.getOrDefault(p.getChallenge(), "reto");
            Instant at = p.getCompletedAt() != null ? p.getCompletedAt() : p.getLastAttemptAt();
            if (at == null) continue;

            String detail = p.isCompleted()
                    ? String.format("Completo \"%s\" (%d pts)", title, p.getScore() == null ? 0 : p.getScore())
                    : String.format("Intento \"%s\"", title);
            events.add(ActivityDTO.builder()
                    .type("CHALLENGE")
                    .user(name)
                    .detail(detail)
                    .at(at)
                    .build());
        }

        for (Survey s : surveyRepository.findAll()) {
            if (s.getCreatedAt() == null) continue;
            String name = userNames.getOrDefault(s.getUser(), "Usuario");
            String detail = String.format("Envio encuesta %s (%d pts)",
                    s.getType(), s.getTotalScore() == null ? 0 : s.getTotalScore());
            events.add(ActivityDTO.builder()
                    .type("SURVEY")
                    .user(name)
                    .detail(detail)
                    .at(s.getCreatedAt())
                    .build());
        }

        events.sort(Comparator.comparing(ActivityDTO::getAt).reversed());
        return events.stream().limit(size).toList();
    }
}
