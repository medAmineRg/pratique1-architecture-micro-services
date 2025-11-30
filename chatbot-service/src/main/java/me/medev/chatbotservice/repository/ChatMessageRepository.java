package me.medev.chatbotservice.repository;

import me.medev.chatbotservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop10ByChatIdOrderByTimestampDesc(String chatId);

    default List<ChatMessage> findTop10ByChatIdOrderByTimestampDescMutable(String chatId) {
        return new ArrayList<>(findTop10ByChatIdOrderByTimestampDesc(chatId));
    }

    @Transactional
    void deleteByChatId(String chatId);
}
