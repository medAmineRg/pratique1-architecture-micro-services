package me.medev.chatbotservice.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.medev.chatbotservice.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private final ChatService chatService;

    @Value("${telegram.bot.token}")
    private String botToken;

    private TelegramClient telegramClient;

    private TelegramClient getTelegramClient() {
        if (telegramClient == null) {
            telegramClient = new OkHttpTelegramClient(botToken);
        }
        return telegramClient;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            try {
                // Handle text messages
                if (message.hasText()) {
                    String userMessage = message.getText();
                    log.info("Received message from chat {}: {}", chatId, userMessage);

                    // Handle commands
                    if (userMessage.startsWith("/")) {
                        handleCommand(chatId, userMessage);
                    } else {
                        // Process with AI
                        String response = chatService.chat(chatId.toString(), userMessage);
                        sendMessage(chatId, response);
                    }
                }
                // Handle document (PDF) uploads
                else if (message.hasDocument()) {
                    String fileName = message.getDocument().getFileName();
                    if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                        String fileId = message.getDocument().getFileId();
                        sendMessage(chatId, "📄 Processing PDF: " + fileName + "...");

                        String result = chatService.processPdfDocument(chatId.toString(), fileId, fileName,
                                getTelegramClient());
                        sendMessage(chatId, result);
                    } else {
                        sendMessage(chatId, "⚠️ Please send a PDF file.");
                    }
                }
            } catch (Exception e) {
                log.error("Error processing message", e);
                sendMessage(chatId, "❌ Error: " + e.getMessage());
            }
        }
    }

    private void handleCommand(Long chatId, String command) {
        String response = switch (command.split(" ")[0].toLowerCase()) {
            case "/start" -> """
                    👋 Welcome to the AI Chatbot!

                    I can help you with:
                    • 💬 Chat with AI - Just send me a message
                    • 📄 RAG - Upload a PDF and ask questions about it
                    • 🔧 Tools - I can query microservices data

                    Commands:
                    /help - Show this help message
                    /clear - Clear conversation history
                    /docs - List uploaded documents
                    """;
            case "/help" -> """
                    📖 Available Commands:
                    /start - Welcome message
                    /help - Show this help
                    /clear - Clear your conversation history
                    /docs - List your uploaded documents

                    💡 Tips:
                    • Send a PDF file to add it to your knowledge base
                    • Ask questions about your uploaded documents
                    • I can also help with general questions using AI
                    """;
            case "/clear" -> {
                chatService.clearHistory(chatId.toString());
                yield "🗑️ Conversation history cleared!";
            }
            case "/docs" -> chatService.listDocuments(chatId.toString());
            default -> "❓ Unknown command. Type /help for available commands.";
        };
        sendMessage(chatId, response);
    }

    private void sendMessage(Long chatId, String text) {
        try {
            // Split long messages (Telegram limit is 4096 chars)
            if (text.length() > 4000) {
                for (int i = 0; i < text.length(); i += 4000) {
                    String chunk = text.substring(i, Math.min(i + 4000, text.length()));
                    SendMessage message = SendMessage.builder()
                            .chatId(chatId.toString())
                            .text(chunk)
                            .build();
                    getTelegramClient().execute(message);
                }
            } else {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text(text)
                        .build();
                getTelegramClient().execute(message);
            }
        } catch (Exception e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }
}
