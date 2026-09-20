package com.computaquest.controller;

import com.computaquest.dto.SurveyDTO;
import com.computaquest.dto.SurveyRequest;
import com.computaquest.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<SurveyDTO> submitSurvey(
            Authentication authentication,
            @Valid @RequestBody SurveyRequest request) {
        return ResponseEntity.ok(surveyService.submitSurvey(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getUserSurveys(Authentication authentication) {
        return ResponseEntity.ok(surveyService.getUserSurveys(authentication.getName()));
    }

    @GetMapping("/latest")
    public ResponseEntity<SurveyDTO> getLatest(
            Authentication authentication,
            @RequestParam String type) {
        return ResponseEntity.ok(surveyService.getLatestByType(authentication.getName(), type));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam String type) {
        return ResponseEntity.ok(surveyService.getSurveyStats(type));
    }

    @GetMapping("/stats/{demographic}")
    public ResponseEntity<Map<String, Object>> getStatsByDemographic(
            @PathVariable String demographic,
            @RequestParam String type) {
        return ResponseEntity.ok(surveyService.getStatsByDemographic(type, demographic));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }
}
