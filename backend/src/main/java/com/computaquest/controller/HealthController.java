package com.computaquest.controller;

import com.computaquest.enums.ChallengeType;
import com.computaquest.enums.Role;
import com.computaquest.model.Challenge;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Value("${app.seed.key:}")
    private String seedKey;

    @Value("${ADMIN_EMAIL:admin@computaquest.com}")
    private String adminEmail;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        try {
            long challengesCount = challengeRepository.count();
            result.put("status", "OK");
            result.put("timestamp", java.time.Instant.now().toString());
            result.put("challengesCount", challengesCount);
            result.put("usersCount", userRepository.count());
        } catch (Exception e) {
            log.error("Error in health check: {}", e.getMessage());
            result.put("status", "ERROR");
            result.put("error", "MongoDB no conectado: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/health/seed")
    public ResponseEntity<Map<String, Object>> seed(
            @RequestHeader(value = "X-Seed-Key", required = false) String providedKey) {
        Map<String, Object> result = new HashMap<>();

        if (seedKey != null && !seedKey.isEmpty()) {
            if (providedKey == null || !providedKey.equals(seedKey)) {
                result.put("status", "FORBIDDEN");
                result.put("message", "Seed key invalida. Envia header X-Seed-Key");
                return ResponseEntity.status(403).body(result);
            }
        }

        try {
            var adminOpt = userRepository.findByEmail(adminEmail);
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                if (admin.getRole() != Role.ADMIN) {
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                    result.put("adminFixed", true);
                    log.info("Admin role fixed to ADMIN for: {}", adminEmail);
                } else {
                    result.put("adminFixed", "already ADMIN");
                }
            } else {
                result.put("adminFixed", "admin not found with email: " + adminEmail);
            }
        } catch (Exception e) {
            result.put("adminError", e.getMessage());
            log.error("Error fixing admin: {}", e.getMessage());
        }

        long count = challengeRepository.count();
        result.put("challengesCountBefore", count);

        if (count == 0) {
            try {
                createChallenge("Descomposicion Alimentaria",
                    "Aprende a descomponer un problema complejo en pasos simples. Ordena los pasos para preparar un sandwich.",
                    ChallengeType.DECOMPOSITION, 1, 100, 10, "Explorador Digital", 1,
                    Map.of("type", "drag-drop",
                        "items", List.of(Map.of("id","1","text","Conseguir el pan"), Map.of("id","2","text","Untar la mayonesa"), Map.of("id","3","text","Agregar el jamon"), Map.of("id","4","text","Cerrar el sandwich")),
                        "correctOrder", List.of("1","2","3","4")));
                createChallenge("Reconocimiento de Patrones",
                    "Identifica patrones logicos en series de numeros. ¿Cual numero sigue en la secuencia?",
                    ChallengeType.PATTERNS, 2, 100, 30, "Maestro de Patrones", 2,
                    Map.of("type", "quiz", "minCorrect", 7,
                        "questions", List.of(
                            Map.of("q","2, 4, 6, 8, ?","a","10","opts",List.of("9","10","11","12")),
                            Map.of("q","5, 10, 15, 20, ?","a","25","opts",List.of("22","23","25","30")),
                            Map.of("q","1, 1, 2, 3, 5, ?","a","8","opts",List.of("6","7","8","9")),
                            Map.of("q","10, 20, 40, 80, ?","a","160","opts",List.of("120","140","160","200")),
                            Map.of("q","100, 90, 80, 70, ?","a","60","opts",List.of("50","55","60","65")),
                            Map.of("q","3, 6, 9, 12, ?","a","15","opts",List.of("13","14","15","16")),
                            Map.of("q","1, 4, 9, 16, ?","a","25","opts",List.of("20","24","25","36")),
                            Map.of("q","2, 6, 18, 54, ?","a","162","opts",List.of("108","162","180","216")),
                            Map.of("q","1, 3, 6, 10, ?","a","15","opts",List.of("12","13","14","15")),
                            Map.of("q","8, 16, 24, 32, ?","a","40","opts",List.of("36","38","40","48")))));
                createChallenge("Abstraccion de Navegacion",
                    "Identifica la informacion relevante en una situacion cotidiana. ¿Que detalles son importantes en un GPS?",
                    ChallengeType.ABSTRACTION, 2, 100, 10, "Experto en Abstraccion", 3,
                    Map.of("type", "multiple-select",
                        "scenario", "Estas usando un GPS para llegar a la escuela. De la siguiente informacion, ¿cuales son RELEVANTES para tu ruta?",
                        "scenarioIcon", "fa-location-dot",
                        "options", List.of("La direccion de destino","El color del auto","El tiempo estimado de llegada","La placa del vehiculo","El trafico en la ruta","La marca del telefono"),
                        "correctAnswers", List.of("La direccion de destino","El tiempo estimado de llegada","El trafico en la ruta")));
                createChallenge("Arquitectura de Algoritmos",
                    "Crea un algoritmo paso a paso para resolver un problema. Ordena los pasos de un programa.",
                    ChallengeType.ALGORITHMS, 2, 100, 10, "Arquitecto de Algoritmos", 4,
                    Map.of("type", "drag-drop",
                        "items", List.of(Map.of("id","1","text","Recibir datos del usuario"), Map.of("id","2","text","Validar la informacion"), Map.of("id","3","text","Procesar los datos"), Map.of("id","4","text","Guardar resultado"), Map.of("id","5","text","Mostrar respuesta")),
                        "correctOrder", List.of("1","2","3","4","5")));
                result.put("challengesCreated", 4);
                log.info("4 challenges seeded successfully");
            } catch (Exception e) {
                result.put("seedError", e.getMessage());
                log.error("Error seeding challenges: {}", e.getMessage(), e);
            }
        } else {
            result.put("challengesAlreadyExist", true);
        }

        result.put("challengesCountAfter", challengeRepository.count());
        result.put("status", "seed-complete");
        return ResponseEntity.ok(result);
    }

    private void createChallenge(String title, String description, ChallengeType type,
                                  int difficulty, int xp, int points, String badge, int order,
                                  Map<String, Object> content) {
        Challenge c = Challenge.builder()
            .title(title)
            .description(description)
            .type(type)
            .difficulty(difficulty)
            .xpReward(xp)
            .pointsReward(points)
            .badgeName(badge)
            .content(content)
            .isActive(true)
            .order(order)
            .build();
        challengeRepository.save(c);
    }
}
