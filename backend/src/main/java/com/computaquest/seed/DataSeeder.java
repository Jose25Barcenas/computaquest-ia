package com.computaquest.seed;

import com.computaquest.enums.ChallengeType;
import com.computaquest.enums.Role;
import com.computaquest.model.Challenge;
import com.computaquest.model.User;
import com.computaquest.repository.ChallengeRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@computaquest.com}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedChallenges();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .avatar("avatar1")
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado exitosamente");
        }
    }

    private void seedChallenges() {
        if (challengeRepository.count() == 0) {
            List<Challenge> challenges = List.of(
                    Challenge.builder()
                            .title("Descomposicion Alimentaria")
                            .description("Aprende a descomponer un problema complejo en pasos simples. Ordena los pasos para preparar un sandwich.")
                            .type(ChallengeType.DECOMPOSITION)
                            .difficulty(1)
                            .xpReward(100)
                            .pointsReward(10)
                            .badgeName("Explorador Digital")
                            .content(Map.of(
                                    "type", "drag-drop",
                                    "items", List.of(
                                            Map.of("id", "1", "text", "Conseguir el pan"),
                                            Map.of("id", "2", "text", "Untar la mayonesa"),
                                            Map.of("id", "3", "text", "Agregar el jamon"),
                                            Map.of("id", "4", "text", "Cerrar el sandwich")
                                    ),
                                    "correctOrder", List.of("1", "2", "3", "4")
                            ))
                            .order(1)
                            .build(),

                    Challenge.builder()
                            .title("Reconocimiento de Patrones")
                            .description("Identifica patrones logicos en series de numeros. ¿Cual numero sigue en la secuencia?")
                            .type(ChallengeType.PATTERNS)
                            .difficulty(2)
                            .xpReward(100)
                            .pointsReward(30)
                            .badgeName("Maestro de Patrones")
                            .content(Map.of(
                                    "type", "quiz",
                                    "minCorrect", 7,
                                    "questions", List.of(
                                            Map.of("q", "2, 4, 6, 8, ?", "a", "10", "opts", List.of("9", "10", "11", "12")),
                                            Map.of("q", "5, 10, 15, 20, ?", "a", "25", "opts", List.of("22", "23", "25", "30")),
                                            Map.of("q", "1, 1, 2, 3, 5, ?", "a", "8", "opts", List.of("6", "7", "8", "9")),
                                            Map.of("q", "10, 20, 40, 80, ?", "a", "160", "opts", List.of("120", "140", "160", "200")),
                                            Map.of("q", "100, 90, 80, 70, ?", "a", "60", "opts", List.of("50", "55", "60", "65")),
                                            Map.of("q", "3, 6, 9, 12, ?", "a", "15", "opts", List.of("13", "14", "15", "16")),
                                            Map.of("q", "1, 4, 9, 16, ?", "a", "25", "opts", List.of("20", "24", "25", "36")),
                                            Map.of("q", "2, 6, 18, 54, ?", "a", "162", "opts", List.of("108", "162", "180", "216")),
                                            Map.of("q", "1, 3, 6, 10, ?", "a", "15", "opts", List.of("12", "13", "14", "15")),
                                            Map.of("q", "8, 16, 24, 32, ?", "a", "40", "opts", List.of("36", "38", "40", "48"))
                                    )
                            ))
                            .order(2)
                            .build(),

                    Challenge.builder()
                            .title("Abstraccion de Navegacion")
                            .description("Identifica la informacion relevante en una situacion cotidiana. ¿Que detalles son importantes en un GPS?")
                            .type(ChallengeType.ABSTRACTION)
                            .difficulty(2)
                            .xpReward(100)
                            .pointsReward(10)
                            .badgeName("Experto en Abstraccion")
                            .content(Map.of(
                                    "type", "multiple-select",
                                    "scenario", "Estas usando un GPS para llegar a la escuela. De la siguiente informacion, ¿cuales son RELEVANTES para tu ruta?",
                                    "scenarioIcon", "fa-location-dot",
                                    "options", List.of(
                                            "La direccion de destino",
                                            "El color del auto",
                                            "El tiempo estimado de llegada",
                                            "La placa del vehiculo",
                                            "El trafico en la ruta",
                                            "La marca del telefono"
                                    ),
                                    "correctAnswers", List.of(
                                            "La direccion de destino",
                                            "El tiempo estimado de llegada",
                                            "El trafico en la ruta"
                                    )
                            ))
                            .order(3)
                            .build(),

                    Challenge.builder()
                            .title("Arquitectura de Algoritmos")
                            .description("Crea un algoritmo paso a paso para resolver un problema. Ordena los pasos de un programa.")
                            .type(ChallengeType.ALGORITHMS)
                            .difficulty(2)
                            .xpReward(100)
                            .pointsReward(10)
                            .badgeName("Arquitecto de Algoritmos")
                            .content(Map.of(
                                    "type", "drag-drop",
                                    "items", List.of(
                                            Map.of("id", "1", "text", "Recibir datos del usuario"),
                                            Map.of("id", "2", "text", "Validar la informacion"),
                                            Map.of("id", "3", "text", "Procesar los datos"),
                                            Map.of("id", "4", "text", "Guardar resultado"),
                                            Map.of("id", "5", "text", "Mostrar respuesta")
                                    ),
                                    "correctOrder", List.of("1", "2", "3", "4", "5")
                            ))
                            .order(4)
                            .build()
            );

            challengeRepository.saveAll(challenges);
            log.info("4 retos educativos creados exitosamente");
        }
    }
}
