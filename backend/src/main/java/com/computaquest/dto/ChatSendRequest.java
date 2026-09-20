package com.computaquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatSendRequest {

    @NotBlank(message = "El mensaje es requerido")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    private String message;

    private String chatId;
    private String challengeType;
}
