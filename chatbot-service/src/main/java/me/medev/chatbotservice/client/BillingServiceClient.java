package me.medev.chatbotservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "BILLING-SERVICE", fallback = BillingServiceClientFallback.class)
public interface BillingServiceClient {

    @GetMapping("/api/bills")
    List<Map<String, Object>> getAllBills();

    @GetMapping("/api/bills/{id}")
    Map<String, Object> getBillById(@PathVariable("id") Long id);
}
