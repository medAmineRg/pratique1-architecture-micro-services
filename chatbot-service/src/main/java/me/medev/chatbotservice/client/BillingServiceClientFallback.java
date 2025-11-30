package me.medev.chatbotservice.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class BillingServiceClientFallback implements BillingServiceClient {

    @Override
    public List<Map<String, Object>> getAllBills() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getBillById(Long id) {
        return Map.of("error", "Billing service unavailable");
    }
}
