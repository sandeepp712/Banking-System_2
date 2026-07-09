package com.bank.banking_api.controller;


import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.Money;
import com.bank.banking_api.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController{
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    //Get account details
    @GetMapping("/{accountNumber}")
    public Account getAccount(@PathVariable String accountNumber){
        return accountService.getAccount(accountNumber);
    }

    //Create new account
    @PostMapping
    public Account createAccount(@RequestParam String accountNumber,
                                 @RequestParam BigDecimal amount){
        Money balance=Money.of(amount, Currency.getInstance("INR"));
        return accountService.createAccount(accountNumber, balance);
    }

    // Deposit in account
    //POST http://localhost:8080/api/accounts/ACC-999/deposit?amount=500
    @PostMapping("/{accountNumber}/deposit")
    public Account deposit(@PathVariable String accountNumber,
                           @RequestParam BigDecimal amount,
                           @RequestParam(required = false) String idempotency_key) {
        // If client didn't send a key, generate one (fallback for simple clients)
        if (idempotency_key == null || idempotency_key.isEmpty()) {
            idempotency_key = "WITHDRAW-" + UUID.randomUUID().toString();
        }

        Money money = Money.of(amount, Currency.getInstance("INR"));
        return accountService.deposit(accountNumber, money,idempotency_key);
    }

    //Withdraw from account
    // POST http://localhost:8080/api/accounts/ACC-999/withdraw?amount=200
    @PostMapping("/{accountNumber}/withdraw")
    public Account withdraw(@PathVariable String accountNumber,
                            @RequestParam BigDecimal amount,
                            @RequestParam(required = false) String idempotency_key) {
        // If client didn't send a key, generate one (fallback for simple clients)
        if (idempotency_key == null || idempotency_key.isEmpty()) {
            idempotency_key = "WITHDRAW-" + UUID.randomUUID().toString();
        }

        Money money = Money.of(amount, Currency.getInstance("INR"));
        return accountService.withdraw(accountNumber, money,idempotency_key);
    }

    //Get all accounts
    @GetMapping("/getAllAccounts")
    public List<Account> getAllAccounts(){
        return accountService.getAllAccounts();
    }
}