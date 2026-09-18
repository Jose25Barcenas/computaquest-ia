package com.computaquest.dto;

import com.computaquest.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String id;
    private String name;
    private String email;
    private String avatar;
    private String grade;
    private Role role;
    private Integer level;
    private Integer xp;
    private Integer points;
    private Integer streak;
    private List<String> badges;
    private Instant createdAt;
}
