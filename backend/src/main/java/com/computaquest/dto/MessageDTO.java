package com.computaquest.dto;

import com.computaquest.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageDTO {
    private MessageRole role;
    private String content;
}
