package com.computaquest.controller;

import com.computaquest.dto.PilotExpertRequest;
import com.computaquest.dto.PilotMotivationRequest;
import com.computaquest.dto.PilotTestRequest;
import com.computaquest.model.PilotExpertReview;
import com.computaquest.model.PilotSession;
import com.computaquest.service.PilotService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pilot")
@RequiredArgsConstructor
public class PilotController {

    private final PilotService pilotService;

    @GetMapping("/session")
    @Operation(summary = "Obtener o crear la sesion del piloto", description = "Modo piloto (adultos). Anonimo por codigo P01, P02...")
    public ResponseEntity<PilotSession> getSession(@RequestParam String code) {
        return ResponseEntity.ok(pilotService.getSession(code));
    }

    @PostMapping("/test")
    @Operation(summary = "Enviar pretest o postest de pensamiento computacional", description = "12 preguntas, 3 por pilar")
    public ResponseEntity<PilotSession> submitTest(@Valid @RequestBody PilotTestRequest request) {
        return ResponseEntity.ok(pilotService.submitTest(request));
    }

    @PostMapping("/motivation")
    @Operation(summary = "Enviar cuestionario de motivacion del piloto", description = "6 items Likert + comentario abierto")
    public ResponseEntity<PilotSession> submitMotivation(@Valid @RequestBody PilotMotivationRequest request) {
        return ResponseEntity.ok(pilotService.submitMotivation(request));
    }

    @PostMapping("/expert")
    @Operation(summary = "Enviar validacion del docente", description = "7 criterios 1-5, comentarios y veredicto")
    public ResponseEntity<PilotExpertReview> submitExpert(@Valid @RequestBody PilotExpertRequest request) {
        return ResponseEntity.ok(pilotService.submitExpertReview(request));
    }
}
