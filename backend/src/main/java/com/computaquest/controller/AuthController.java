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
}
