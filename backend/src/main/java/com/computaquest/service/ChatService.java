package com.computaquest.service;

import com.computaquest.config.OpenAiConfig;
import com.computaquest.dto.*;
import com.computaquest.enums.MessageRole;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.model.Chat;
import com.computaquest.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final OpenAiConfig openAiConfig;

    private static final String SYSTEM_PROMPT = """
            Eres el Tutor de ComputaQuest IA, una plataforma educativa que enseña pensamiento computacional a estudiantes de secundaria.
            
            Tus pilares de enseñanza son:
            1. **Descomposición**: Dividir problemas complejos en partes más pequeñas
            2. **Reconocimiento de Patrones**: Identificar similitudes y tendencias
            3. **Abstracción**: Enfocarse en lo importante ignorando detalles irrelevantes
            4. **Algoritmos**: Crear pasos paso a paso para resolver problemas
            
            Reglas:
            - Responde en español
            - Sé amigable y usa un tono adecuado para adolescentes
            - Mantén tus respuestas en 3-4 oraciones máximo
            - Relaciona los conceptos con situaciones cotidianas
            - Si te preguntan algo fuera de contexto, redirige suavemente al pensamiento computacional
            """;

    public ChatResponse sendMessage(String userId, ChatSendRequest request) {
        Chat chat = null;

        if (request.getChatId() != null && !request.getChatId().isEmpty()) {
            chat = chatRepository.findByIdAndUser(request.getChatId(), userId)
                    .orElse(null);
        }

        if (chat == null) {
            chat = Chat.builder()
                    .user(userId)
                    .challengeType(request.getChallengeType())
                    .messages(new ArrayList<>())
                    .createdAt(Instant.now())
                    .build();
        }

        chat.getMessages().add(Chat.ChatMessage.builder()
                .role(MessageRole.USER)
                .content(request.getMessage())
                .build());

        String aiResponse = callOpenAI(chat);

        chat.getMessages().add(Chat.ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content(aiResponse)
                .build());

        chat = chatRepository.save(chat);

        return ChatResponse.builder()
                .chatId(chat.getId())
                .message(aiResponse)
                .build();
    }

    public List<MessageDTO> getChatHistory(String userId, String chatId) {
        Chat chat = chatRepository.findByIdAndUser(chatId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat no encontrado"));

        return chat.getMessages().stream()
                .map(msg -> MessageDTO.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .toList();
    }

    public List<ChatDTO> getUserChats(String userId) {
        List<Chat> chats = chatRepository.findByUserOrderByCreatedAtDesc(userId);

        return chats.stream().map(chat -> {
            String lastMessage = chat.getMessages().isEmpty() ? "" :
                    chat.getMessages().get(chat.getMessages().size() - 1).getContent();

            return ChatDTO.builder()
                    .id(chat.getId())
                    .challengeType(chat.getChallengeType())
                    .createdAt(chat.getCreatedAt())
                    .messageCount(chat.getMessages().size())
                    .lastMessage(lastMessage.length() > 100 ? lastMessage.substring(0, 100) + "..." : lastMessage)
                    .build();
        }).toList();
    }

    private String callOpenAI(Chat chat) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

            List<Chat.ChatMessage> recentMessages = chat.getMessages().size() > 10 ?
                    chat.getMessages().subList(chat.getMessages().size() - 10, chat.getMessages().size()) :
                    chat.getMessages();

            for (Chat.ChatMessage msg : recentMessages) {
                messages.add(Map.of("role", msg.getRole().getValue(), "content", msg.getContent()));
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", openAiConfig.getMaxTokens());
            requestBody.put("temperature", openAiConfig.getTemperature());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "Lo siento, no pude procesar tu mensaje en este momento. Intenta de nuevo.";

        } catch (Exception e) {
            log.error("Error al llamar a OpenAI API: {}", e.getMessage());
            return "Lo siento, el tutor IA no está disponible en este momento. Por favor, intenta más tarde.";
        }
    }
}
