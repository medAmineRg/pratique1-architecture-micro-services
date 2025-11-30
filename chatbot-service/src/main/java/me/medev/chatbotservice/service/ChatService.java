package me.medev.chatbotservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.medev.chatbotservice.entity.ChatMessage;
import me.medev.chatbotservice.entity.Document;
import me.medev.chatbotservice.repository.ChatMessageRepository;
import me.medev.chatbotservice.repository.DocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentRepository documentRepository;
    private final RagService ragService;
    private final McpToolService mcpToolService;

    public String chat(String chatId, String userMessage) {
        log.info("Processing chat for user {}: {}", chatId, userMessage);

        // Save user message
        saveMessage(chatId, "user", userMessage);

        // Build conversation history
        List<Message> messages = buildConversation(chatId, userMessage);

        // Get AI response
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt(new Prompt(messages))
                .call()
                .content();

        // Save assistant response
        saveMessage(chatId, "assistant", response);

        return response;
    }

    private List<Message> buildConversation(String chatId, String currentMessage) {
        List<Message> messages = new ArrayList<>();

        // System prompt with RAG context and MCP tools info
        String systemPrompt = buildSystemPrompt(chatId, currentMessage);
        messages.add(new SystemMessage(systemPrompt));

        // Add conversation history (last 10 messages)
        List<ChatMessage> history = chatMessageRepository
                .findTop10ByChatIdOrderByTimestampDescMutable(chatId);
        Collections.reverse(history);

        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Add current message
        messages.add(new UserMessage(currentMessage));

        return messages;
    }

    private String buildSystemPrompt(String chatId, String userMessage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are a helpful AI assistant integrated with a microservices architecture.
                You can help users with general questions and also answer questions based on
                documents they have uploaded (RAG system).

                Be concise and helpful. Use emojis sparingly to make responses more engaging.

                """);

        // Add RAG context if documents exist
        String ragContext = ragService.searchRelevantContext(chatId, userMessage);
        if (ragContext != null && !ragContext.isEmpty()) {
            prompt.append("\n--- RELEVANT DOCUMENT CONTEXT ---\n");
            prompt.append(ragContext);
            prompt.append("\n--- END OF CONTEXT ---\n");
            prompt.append("\nUse the above context to answer questions when relevant.\n");
        }

        // Add MCP tools information
        String toolsInfo = mcpToolService.getAvailableToolsDescription();
        prompt.append("\n").append(toolsInfo);

        return prompt.toString();
    }

    public String processPdfDocument(String chatId, String fileId, String fileName, TelegramClient telegramClient) {
        try {
            return ragService.processPdfFromTelegram(chatId, fileId, fileName, telegramClient);
        } catch (Exception e) {
            log.error("Error processing PDF", e);
            return "❌ Failed to process PDF: " + e.getMessage();
        }
    }

    public void clearHistory(String chatId) {
        chatMessageRepository.deleteByChatId(chatId);
        log.info("Cleared chat history for {}", chatId);
    }

    public String listDocuments(String chatId) {
        List<Document> docs = documentRepository.findByChatId(chatId);
        if (docs.isEmpty()) {
            return "📭 No documents uploaded yet.\n\nSend me a PDF file to get started!";
        }

        StringBuilder sb = new StringBuilder("📚 Your Documents:\n\n");
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            sb.append(String.format("%d. %s\n   📅 %s\n   📝 %d chunks\n\n",
                    i + 1,
                    doc.getFileName(),
                    doc.getUploadedAt().toLocalDate(),
                    doc.getChunkCount()));
        }
        return sb.toString();
    }

    private void saveMessage(String chatId, String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .chatId(chatId)
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(message);
    }
}
