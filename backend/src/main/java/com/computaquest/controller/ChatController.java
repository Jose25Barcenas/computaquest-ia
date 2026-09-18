package com.computaquest.controller;

import com.computaquest.dto.*;
import com.computaquest.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody ChatSendRequest request) {
        String userId = authentication.getName();
        return ResponseEntity.ok(chatService.sendMessage(userId, request));
    }

    @GetMapping("/history/{chatId}")
    public ResponseEntity<List<MessageDTO>> getChatHistory(
            Authentication authentication,
            @PathVariable String chatId) {
        String userId = authentication.getName();
        return ResponseEntity.ok(chatService.getChatHistory(userId, chatId));
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatDTO>> getUserChats(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(chatService.getUserChats(userId));
    }
}
