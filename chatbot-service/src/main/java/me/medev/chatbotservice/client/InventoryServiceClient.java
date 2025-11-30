package me.medev.chatbotservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "INVENTORY-SERVICE", fallback = InventoryServiceClientFallback.class)
public interface InventoryServiceClient {

    @GetMapping("/api/products")
    List<Map<String, Object>> getAllProducts();

    @GetMapping("/api/products/{id}")
    Map<String, Object> getProductById(@PathVariable("id") Long id);
}
