package com.computaquest.service;

import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.model.User;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toPublicMap).toList();
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    private Map<String, Object> toPublicMap(User user) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("_id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("avatar", user.getAvatar());
        map.put("role", user.getRole().getValue());
        map.put("level", user.getLevel());
        map.put("xp", user.getXp());
        map.put("points", user.getPoints());
        map.put("streak", user.getStreak());
        map.put("badges", user.getBadges());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}
