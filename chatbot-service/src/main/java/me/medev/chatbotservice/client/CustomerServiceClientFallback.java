package me.medev.chatbotservice.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CustomerServiceClientFallback implements CustomerServiceClient {

    @Override
    public List<Map<String, Object>> getAllCustomers() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getCustomerById(Long id) {
        return Map.of("error", "Customer service unavailable");
    }
}
