package com.computaquest.controller;

import com.computaquest.dto.ActivityDTO;
import com.computaquest.dto.AdminStatsDTO;
import com.computaquest.dto.ChallengeStatsDTO;
import com.computaquest.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadisticas del panel", description = "KPIs generales para el panel de administracion (solo Admin)")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }

    @GetMapping("/challenge-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rendimiento por reto", description = "Completados, intentos, puntaje promedio y tasa de finalizacion por reto (solo Admin)")
    public ResponseEntity<List<ChallengeStatsDTO>> getChallengeStats() {
        return ResponseEntity.ok(adminStatsService.getChallengeStats());
    }

    @GetMapping("/activity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actividad reciente", description = "Ultimos eventos de retos y encuestas (solo Admin)")
    public ResponseEntity<List<ActivityDTO>> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminStatsService.getRecentActivity(limit));
    }
}
