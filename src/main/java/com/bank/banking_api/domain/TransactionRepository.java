package com.bank.banking_api.domain;

public interface TransactionRepository {
    void save(Transaction transaction);
    boolean existsByIdempotencyKey(String key);
}