package me.medev.chatbotservice.config;

import me.medev.chatbotservice.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(TelegramBot telegramBot) throws Exception {
        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(botToken, telegramBot);
        return botsApplication;
    }
}
