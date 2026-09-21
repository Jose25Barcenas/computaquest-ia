package com.computaquest.service;

import com.computaquest.dto.*;
import com.computaquest.enums.Role;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.User;
import com.computaquest.repository.UserRepository;
import com.computaquest.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${ADMIN_EMAIL:admin@computaquest.com}")
    private String adminEmail;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationAppException("Ya existe una cuenta con este email");
        }

        Role assignedRole = request.getEmail().equalsIgnoreCase(adminEmail) ? Role.ADMIN : Role.STUDENT;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatar(request.getAvatar() != null ? request.getAvatar() : "avatar1")
                .grade(request.getGrade())
                .role(assignedRole)
                .build();

        user = userRepository.save(user);
        log.info("Nuevo usuario registrado: {} con rol: {}", user.getEmail(), assignedRole);

        String token = tokenProvider.generateToken(user.getEmail());

        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setLastLoginDate(Instant.now());
        userRepository.save(user);

        log.info("Login exitoso: {}", user.getEmail());

        String token = tokenProvider.generateToken(authentication);

        return buildAuthResponse(user, token);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public AuthResponse getCurrentUserResponse(String email) {
        User user = getCurrentUser(email);
        String token = tokenProvider.generateToken(user.getEmail());
        return buildAuthResponse(user, token);
    }

    public AuthResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = getCurrentUser(email);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        log.info("Perfil actualizado: {}", email);

        String token = tokenProvider.generateToken(user.getEmail());
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .grade(user.getGrade())
                .role(user.getRole())
                .level(user.getLevel())
                .xp(user.getXp())
                .points(user.getPoints())
                .streak(user.getStreak())
                .badges(user.getBadges())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cuenta con este email"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(Instant.now().plusSeconds(3600));
        userRepository.save(user);

        log.info("Token de restablecimiento generado para: {}", email);

        return resetToken;
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ValidationAppException("Token invalido o expirado"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new ValidationAppException("Token invalido o expirado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Contrasena restablecida para: {}", user.getEmail());
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationAppException("La contrasena actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Contrasena cambiada para: {}", email);
    }
}
