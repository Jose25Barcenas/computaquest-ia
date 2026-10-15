package com.computaquest.controller;

import com.computaquest.service.PilotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pilot")
@RequiredArgsConstructor
public class PilotExportController {

    private final PilotService pilotService;

    @GetMapping("/export.csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar resultados del piloto", description = "CSV con una fila por participante (solo Admin)")
    public ResponseEntity<String> exportParticipants() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"piloto-resultados.csv\"")
                .body(pilotService.exportParticipantsCsv());
    }

    @GetMapping("/export-experto.csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar validacion del docente", description = "CSV con las respuestas del experto (solo Admin)")
    public ResponseEntity<String> exportExpert() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"piloto-validacion-docente.csv\"")
                .body(pilotService.exportExpertCsv());
    }
}
