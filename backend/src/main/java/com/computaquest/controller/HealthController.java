package com.computaquest.controller;

import com.computaquest.repository.ChallengeRepository;
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

    private final ChallengeRepository challengeRepository;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Sondeo de BD: si falla, se reporta 503 (los contadores no se exponen en un endpoint publico)
            challengeRepository.count();
            result.put("status", "OK");
            result.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in health check: {}", e.getMessage());
            result.put("status", "ERROR");
            result.put("error", "Database connection failed");
            return ResponseEntity.status(503).body(result);
        }
    }
}
