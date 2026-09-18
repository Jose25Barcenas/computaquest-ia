package com.computaquest.controller;

import com.computaquest.dto.CompleteChallengeRequest;
import com.computaquest.dto.LeaderboardEntry;
import com.computaquest.dto.ProgressDTO;
import com.computaquest.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Progreso y gamificacion de usuarios")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    @Operation(summary = "Obtener progreso", description = "Retorna el progreso del usuario autenticado")
    public ResponseEntity<List<ProgressDTO>> getProgress(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }

    @PostMapping("/complete")
    @Operation(summary = "Completar reto", description = "Registra la finalizacion de un reto y otorga recompensas")
    public ResponseEntity<ProgressDTO> completeChallenge(
            Authentication authentication,
            @Valid @RequestBody CompleteChallengeRequest request) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(progressService.completeChallenge(userId, request));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Leaderboard", description = "Tabla de clasificacion de todos los usuarios")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(progressService.getLeaderboard());
    }

    private String getUserId(Authentication authentication) {
        return authentication.getName();
    }
}
