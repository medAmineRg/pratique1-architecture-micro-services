package me.medev.chatbotservice.repository;

import me.medev.chatbotservice.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByChatId(String chatId);

    List<DocumentChunk> findByDocumentId(Long documentId);
}
