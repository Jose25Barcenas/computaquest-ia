package com.computaquest.dto;

import com.computaquest.enums.ChallengeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class ChallengeCreateRequest {

    @NotBlank(message = "El título es requerido")
    @Size(max = 100, message = "El título no puede tener más de 100 caracteres")
    private String title;

    @NotBlank(message = "La descripción es requerida")
    private String description;

    @NotNull(message = "El tipo es requerido")
    private ChallengeType type;

    private Integer difficulty;
    private Integer xpReward;
    private Integer pointsReward;
    private String badgeName;

    @NotNull(message = "El contenido es requerido")
    @Size(max = 50000, message = "El contenido no puede exceder 50KB")
    private Map<String, Object> content;

    private Boolean isActive;

    private Integer order;
}
