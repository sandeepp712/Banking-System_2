package com.bank.banking_api.controller;

import com.bank.banking_api.domain.Money;
import com.bank.banking_api.service.TransferService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // Transfer from A to B
    // POST http://localhost:8080/api/transfers?from=ACC-1&to=ACC-2&amount=100
    @PostMapping
    public String transfer(@RequestParam String from,
                           @RequestParam String to,
                           @RequestParam BigDecimal amount) {
        Money money = Money.of(amount, Currency.getInstance("INR"));
        transferService.transfer(from, to, money);
        return "Transfer successful";
    }
}