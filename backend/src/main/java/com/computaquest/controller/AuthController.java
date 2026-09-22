package com.computaquest.controller;

import com.computaquest.dto.*;
import com.computaquest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacion y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil", description = "Retorna el perfil del usuario autenticado")
    public ResponseEntity<AuthResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUserResponse(authentication.getName()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Actualizar perfil", description = "Actualiza nombre y avatar del usuario")
    public ResponseEntity<AuthResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Olvide mi contrasena", description = "Genera un token de restablecimiento para el email proporcionado")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Si existe una cuenta con ese email, recibiras un enlace para restablecer tu contrasena."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contrasena", description = "Restablece la contrasena usando un token valido")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contrasena restablecida exitosamente"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contrasena", description = "Cambia la contrasena del usuario autenticado")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Contrasena cambiada exitosamente"));
    }
}
