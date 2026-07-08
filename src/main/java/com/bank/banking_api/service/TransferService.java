package com.bank.banking_api.service;

import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.Money;
import com.bank.banking_api.persistence.JdbcAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final JdbcAccountRepository jdbcAccountRepository;

    public TransferService(JdbcAccountRepository jdbcAccountRepository) {
        this.jdbcAccountRepository = jdbcAccountRepository;
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, Money amount) {
        Account from = jdbcAccountRepository.findByAccountNumber(fromAccountId).orElseThrow(() -> new IllegalArgumentException("Sending account not found"));
        Account to = jdbcAccountRepository.findByAccountNumber(toAccountId).orElseThrow(() -> new IllegalArgumentException("Sending account not found"));

        from.debit(amount);
        to.debit(amount);

        jdbcAccountRepository.update(from);
        jdbcAccountRepository.update(to);
    }
}