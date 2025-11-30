package me.medev.chatbotservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.medev.chatbotservice.client.CustomerServiceClient;
import me.medev.chatbotservice.client.InventoryServiceClient;
import me.medev.chatbotservice.client.BillingServiceClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP (Model Context Protocol) Tool Service
 * Provides tools that the AI can use to query microservices
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {

    private final CustomerServiceClient customerClient;
    private final InventoryServiceClient inventoryClient;
    private final BillingServiceClient billingClient;

    public String getAvailableToolsDescription() {
        return """

                AVAILABLE TOOLS (MCP Integration):
                You have access to the following microservices data:

                1. CUSTOMERS: Query customer information
                   - List all customers
                   - Get customer by ID

                2. PRODUCTS/INVENTORY: Query product catalog
                   - List all products
                   - Get product by ID

                3. BILLS: Query billing information
                   - List all bills
                   - Get bill by ID

                When the user asks about customers, products, or bills, you can describe
                what data is available but mention they should use the web interface for
                detailed queries.
                """;
    }

    public String executeQuery(String toolName, Map<String, Object> params) {
        log.info("Executing MCP tool: {} with params: {}", toolName, params);

        try {
            return switch (toolName.toLowerCase()) {
                case "list_customers" -> listCustomers();
                case "get_customer" -> getCustomer(params);
                case "list_products" -> listProducts();
                case "get_product" -> getProduct(params);
                case "list_bills" -> listBills();
                case "get_bill" -> getBill(params);
                default -> "Unknown tool: " + toolName;
            };
        } catch (Exception e) {
            log.error("Error executing tool {}", toolName, e);
            return "Error querying " + toolName + ": " + e.getMessage();
        }
    }

    private String listCustomers() {
        try {
            List<?> customers = customerClient.getAllCustomers();
            return "Found " + customers.size() + " customers:\n" + formatList(customers);
        } catch (Exception e) {
            return "Unable to fetch customers: " + e.getMessage();
        }
    }

    private String getCustomer(Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        try {
            Object customer = customerClient.getCustomerById(id);
            return "Customer: " + customer.toString();
        } catch (Exception e) {
            return "Customer not found: " + e.getMessage();
        }
    }

    private String listProducts() {
        try {
            List<?> products = inventoryClient.getAllProducts();
            return "Found " + products.size() + " products:\n" + formatList(products);
        } catch (Exception e) {
            return "Unable to fetch products: " + e.getMessage();
        }
    }

    private String getProduct(Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        try {
            Object product = inventoryClient.getProductById(id);
            return "Product: " + product.toString();
        } catch (Exception e) {
            return "Product not found: " + e.getMessage();
        }
    }

    private String listBills() {
        try {
            List<?> bills = billingClient.getAllBills();
            return "Found " + bills.size() + " bills:\n" + formatList(bills);
        } catch (Exception e) {
            return "Unable to fetch bills: " + e.getMessage();
        }
    }

    private String getBill(Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        try {
            Object bill = billingClient.getBillById(id);
            return "Bill: " + bill.toString();
        } catch (Exception e) {
            return "Bill not found: " + e.getMessage();
        }
    }

    private String formatList(List<?> items) {
        if (items.isEmpty())
            return "No items found.";
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Object item : items) {
            if (count++ >= 5) {
                sb.append("... and ").append(items.size() - 5).append(" more\n");
                break;
            }
            sb.append("• ").append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}
