package me.medev.chatbotservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.medev.chatbotservice.entity.Document;
import me.medev.chatbotservice.entity.DocumentChunk;
import me.medev.chatbotservice.repository.DocumentChunkRepository;
import me.medev.chatbotservice.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;

    @Value("${telegram.bot.token}")
    private String botToken;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    public String processPdfFromTelegram(String chatId, String fileId, String fileName, TelegramClient telegramClient)
            throws Exception {
        log.info("Processing PDF {} for chat {}", fileName, chatId);

        // Get file path from Telegram
        GetFile getFile = new GetFile(fileId);
        File file = telegramClient.execute(getFile);
        String filePath = file.getFilePath();
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;

        // Download and extract text from PDF
        String pdfText = extractTextFromPdf(fileUrl);
        if (pdfText.isEmpty()) {
            return "⚠️ Could not extract text from the PDF. It might be scanned or image-based.";
        }

        // Split into chunks
        List<String> chunks = splitIntoChunks(pdfText);
        log.info("Split PDF into {} chunks", chunks.size());

        // Save document
        Document document = Document.builder()
                .chatId(chatId)
                .fileName(fileName)
                .uploadedAt(LocalDateTime.now())
                .chunkCount(chunks.size())
                .build();
        document = documentRepository.save(document);

        // Generate embeddings and save chunks
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = generateEmbedding(chunk);

            DocumentChunk docChunk = DocumentChunk.builder()
                    .document(document)
                    .chatId(chatId)
                    .chunkIndex(i)
                    .content(chunk)
                    .embedding(embedding)
                    .build();
            chunkRepository.save(docChunk);
        }

        log.info("Successfully processed PDF {} with {} chunks", fileName, chunks.size());
        return String.format(
                "✅ Successfully processed **%s**\n\n📊 Extracted %d text chunks\n\n💡 You can now ask questions about this document!",
                fileName, chunks.size());
    }

    private String extractTextFromPdf(String fileUrl) throws Exception {
        try (InputStream is = URI.create(fileUrl).toURL().openStream();
                PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;

        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;

            if (wordCount >= CHUNK_SIZE) {
                chunks.add(currentChunk.toString().trim());

                // Keep overlap
                String[] currentWords = currentChunk.toString().split("\\s+");
                currentChunk = new StringBuilder();
                for (int i = Math.max(0, currentWords.length - CHUNK_OVERLAP); i < currentWords.length; i++) {
                    currentChunk.append(currentWords[i]).append(" ");
                }
                wordCount = CHUNK_OVERLAP;
            }
        }

        if (!currentChunk.toString().trim().isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private float[] generateEmbedding(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            log.warn("Failed to generate embedding, using empty array", e);
            return new float[0];
        }
    }

    public String searchRelevantContext(String chatId, String query) {
        List<DocumentChunk> allChunks = chunkRepository.findByChatId(chatId);
        if (allChunks.isEmpty()) {
            return "";
        }

        // Generate query embedding
        float[] queryEmbedding = generateEmbedding(query);
        if (queryEmbedding.length == 0) {
            // Fallback to simple keyword search if embedding fails
            return simpleKeywordSearch(allChunks, query);
        }

        // Calculate similarity and get top results
        List<ChunkWithScore> scoredChunks = allChunks.stream()
                .filter(chunk -> chunk.getEmbedding() != null && chunk.getEmbedding().length > 0)
                .map(chunk -> new ChunkWithScore(chunk, cosineSimilarity(queryEmbedding, chunk.getEmbedding())))
                .sorted(Comparator.comparingDouble(ChunkWithScore::score).reversed())
                .limit(3)
                .toList();

        if (scoredChunks.isEmpty()) {
            return simpleKeywordSearch(allChunks, query);
        }

        StringBuilder context = new StringBuilder();
        for (ChunkWithScore scored : scoredChunks) {
            if (scored.score > 0.3) { // Threshold for relevance
                context.append(scored.chunk.getContent()).append("\n\n");
            }
        }

        return context.toString();
    }

    private String simpleKeywordSearch(List<DocumentChunk> chunks, String query) {
        String[] keywords = query.toLowerCase().split("\\s+");

        return chunks.stream()
                .filter(chunk -> {
                    String content = chunk.getContent().toLowerCase();
                    for (String keyword : keywords) {
                        if (keyword.length() > 3 && content.contains(keyword)) {
                            return true;
                        }
                    }
                    return false;
                })
                .limit(3)
                .map(DocumentChunk::getContent)
                .reduce("", (a, b) -> a + "\n\n" + b);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length || a.length == 0)
            return 0;

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }

    private record ChunkWithScore(DocumentChunk chunk, double score) {
    }
}
