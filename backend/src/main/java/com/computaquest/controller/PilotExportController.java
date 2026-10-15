package com.computaquest.controller;

import com.computaquest.service.PilotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @DeleteMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Borrar una sesion del piloto", description = "Borra por codigo, ej. P09 (solo Admin)")
    public ResponseEntity<Map<String, String>> deleteSession(@RequestParam String code) {
        pilotService.deleteSession(code);
        return ResponseEntity.ok(Map.of("deleted", code.trim().toUpperCase(java.util.Locale.ROOT)));
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
