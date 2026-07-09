package com.bank.banking_api.service;

import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.Money;
import com.bank.banking_api.domain.Transaction;
import com.bank.banking_api.persistence.JdbcAccountRepository;
import com.bank.banking_api.persistence.JdbcTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {
    private final JdbcAccountRepository jdbcAccountRepository;
    private final JdbcTransactionRepository transactionRepository;

    public TransferService(JdbcAccountRepository jdbcAccountRepository,JdbcTransactionRepository  transactionRepository) {
        this.jdbcAccountRepository = jdbcAccountRepository;
        this.transactionRepository=transactionRepository;
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, Money amount,String idempotencyKey) {

        //Idempotency check : stop double spending
        if(transactionRepository.existsByIdempotencyKey(idempotencyKey)){
            throw new IllegalArgumentException("Transaction with idempotency key already exists");
        }

        Account from = jdbcAccountRepository.findByAccountNumber(fromAccountId).orElseThrow(() -> new IllegalArgumentException("Sending account not found"));
        Account to = jdbcAccountRepository.findByAccountNumber(toAccountId).orElseThrow(() -> new IllegalArgumentException("Sending account not found"));

        from.debit(amount);
        to.debit(amount);

        jdbcAccountRepository.update(from);
        jdbcAccountRepository.update(to);

        Transaction tx=new Transaction("TRANSFER",fromAccountId,toAccountId,amount,idempotencyKey);
        transactionRepository.save(tx);
    }
}