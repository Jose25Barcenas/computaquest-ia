package com.computaquest.service;

import com.computaquest.dto.AdminStatsDTO;
import com.computaquest.enums.Role;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.SurveyRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
}
