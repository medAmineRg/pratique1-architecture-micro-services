package me.medev.billingservice.controller;

import me.medev.billingservice.client.CustomerServiceClient;
import me.medev.billingservice.client.ProductServiceClient;
import me.medev.billingservice.dto.CustomerDto;
import me.medev.billingservice.dto.ProductDto;
import me.medev.billingservice.entity.Bill;
import me.medev.billingservice.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @GetMapping
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBillById(@PathVariable Long id) {
        Optional<Bill> billOpt = billRepository.findById(id);
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("bill", bill);

            try {
                CustomerDto customer = customerServiceClient.getCustomerById(bill.getCustomerId());
                response.put("customer", customer);
            } catch (Exception e) {
                response.put("customerError", "Could not fetch customer details");
            }

            try {
                ProductDto product = productServiceClient.getProductById(bill.getProductId());
                response.put("product", product);
            } catch (Exception e) {
                response.put("productError", "Could not fetch product details");
            }

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/customer/{customerId}")
    public List<Bill> getBillsByCustomer(@PathVariable Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        try {
            // Fetch product to calculate total
            ProductDto product = productServiceClient.getProductById(bill.getProductId());
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(bill.getQuantity()));
            bill.setTotalAmount(totalAmount);

            // Verify customer exists
            customerServiceClient.getCustomerById(bill.getCustomerId());

            return ResponseEntity.ok(billRepository.save(bill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        if (billRepository.existsById(id)) {
            billRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}