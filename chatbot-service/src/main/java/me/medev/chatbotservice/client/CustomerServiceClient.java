package me.medev.chatbotservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "CUSTOMER-SERVICE", fallback = CustomerServiceClientFallback.class)
public interface CustomerServiceClient {

    @GetMapping("/api/customers")
    List<Map<String, Object>> getAllCustomers();

    @GetMapping("/api/customers/{id}")
    Map<String, Object> getCustomerById(@PathVariable("id") Long id);
}
