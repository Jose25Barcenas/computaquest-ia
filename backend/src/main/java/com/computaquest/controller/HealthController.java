package com.computaquest.controller;

import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("status", "OK");
            result.put("timestamp", java.time.Instant.now().toString());
            result.put("challengesCount", challengeRepository.count());
            result.put("usersCount", userRepository.count());
        } catch (Exception e) {
            log.error("Error in health check: {}", e.getMessage());
            result.put("status", "ERROR");
            result.put("error", "MongoDB no conectado: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
