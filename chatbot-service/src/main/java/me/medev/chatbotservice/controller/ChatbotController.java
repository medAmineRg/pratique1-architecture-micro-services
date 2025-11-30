package me.medev.chatbotservice.controller;

import lombok.RequiredArgsConstructor;
import me.medev.chatbotservice.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatService chatService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "chatbot-service"));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String chatId = request.getOrDefault("chatId", "web-user");
        String message = request.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String response = chatService.chat(chatId, message);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @DeleteMapping("/history/{chatId}")
    public ResponseEntity<Map<String, String>> clearHistory(@PathVariable String chatId) {
        chatService.clearHistory(chatId);
        return ResponseEntity.ok(Map.of("message", "History cleared"));
    }

    @GetMapping("/documents/{chatId}")
    public ResponseEntity<String> listDocuments(@PathVariable String chatId) {
        return ResponseEntity.ok(chatService.listDocuments(chatId));
    }
}
