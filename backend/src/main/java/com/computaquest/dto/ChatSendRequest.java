package com.computaquest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatSendRequest {

    @NotBlank(message = "El mensaje es requerido")
    private String message;

    private String chatId;
    private String challengeType;
}
