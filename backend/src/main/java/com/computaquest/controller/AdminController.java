package com.computaquest.controller;

import com.computaquest.dto.AdminStatsDTO;
import com.computaquest.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
