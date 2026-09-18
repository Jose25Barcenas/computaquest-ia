package com.computaquest.controller;

import com.computaquest.dto.ChallengeCreateRequest;
import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenges", description = "CRUD de retos educativos")
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    @Operation(summary = "Listar retos", description = "Obtiene todos los retos activos, opcionalmente filtrados por tipo y dificultad")
    public ResponseEntity<List<ChallengeDTO>> getChallenges(
            @RequestParam(required = false) ChallengeType type,
            @RequestParam(required = false) Integer difficulty) {
        return ResponseEntity.ok(challengeService.getAllChallenges(type, difficulty));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reto", description = "Obtiene un reto por su ID")
    public ResponseEntity<ChallengeDTO> getChallenge(@PathVariable String id) {
        return ResponseEntity.ok(challengeService.getChallengeById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear reto", description = "Crea un nuevo reto educativo (solo Admin)")
    public ResponseEntity<ChallengeDTO> createChallenge(@Valid @RequestBody ChallengeCreateRequest request) {
        return ResponseEntity.ok(challengeService.createChallenge(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar reto", description = "Actualiza un reto existente (solo Admin)")
    public ResponseEntity<ChallengeDTO> updateChallenge(
            @PathVariable String id,
            @Valid @RequestBody ChallengeCreateRequest request) {
        return ResponseEntity.ok(challengeService.updateChallenge(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar reto", description = "Elimina un reto (solo Admin)")
    public ResponseEntity<Void> deleteChallenge(@PathVariable String id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.noContent().build();
    }
}
