package com.computaquest;

import com.computaquest.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Blindaje de la superficie publica/privada de la API:
 * todo lo que no este en la lista de permitAll DEBE devolver 401 sin token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithGarbageTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfileWithoutTokenReturns401() throws Exception {
        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Hacker"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "a", "newPassword", "b"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createChallengeWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "x", "description", "y", "type", "decomposition",
                                "content", Map.of("type", "quiz", "questions", List.of())))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leaderboardWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/progress/leaderboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void challengeListIsPublic() throws Exception {
        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk());
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void loginAndRegisterRemainPublic() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Security Test");
        request.setEmail("sec-" + UUID.randomUUID() + "@example.com");
        request.setPassword("password123");
        request.setGrade("8");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPasswordWithoutMailConfiguredReturns503() throws Exception {
        // Crea una cuenta para que el flujo intente enviar el correo
        RegisterRequest request = new RegisterRequest();
        request.setName("Mail Test");
        String email = "mail-" + UUID.randomUUID() + "@example.com";
        request.setEmail(email);
        request.setPassword("password123");
        request.setGrade("8");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Sin spring.mail.host configurado el servidor debe fallar ruidosamente (503),
        // nunca prometer un email que no se envia
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void forgotPasswordWithUnknownEmailReturnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "nadie-" + UUID.randomUUID() + "@example.com"))))
                .andExpect(status().isOk());
    }
}
