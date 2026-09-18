package com.computaquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ChatDTO {
    private String id;
    private String challengeType;
    private Instant createdAt;
    private Integer messageCount;
    private String lastMessage;
}
