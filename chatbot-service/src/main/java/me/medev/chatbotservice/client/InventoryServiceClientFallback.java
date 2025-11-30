package me.medev.chatbotservice.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class InventoryServiceClientFallback implements InventoryServiceClient {

    @Override
    public List<Map<String, Object>> getAllProducts() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getProductById(Long id) {
        return Map.of("error", "Inventory service unavailable");
    }
}
