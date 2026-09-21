package com.computaquest.service;

import com.computaquest.dto.UserDTO;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.model.User;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDTO).toList();
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
        log.info("Usuario eliminado: {}", id);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .level(user.getLevel())
                .xp(user.getXp())
                .points(user.getPoints())
                .streak(user.getStreak())
                .badges(user.getBadges())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
