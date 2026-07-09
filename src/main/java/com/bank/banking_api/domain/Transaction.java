package com.bank.banking_api.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Transaction {
    private final UUID id;
    private final String type;
    private final String fromAccountId;
    private final String toAccountId;
    private final Money amount;
    private final String status;
    private final String idempotencyKey;
    private final Instant createdAt;

    public Transaction(String type, String fromAccountId, String toAccountId, Money amount, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = "COMMITTED";
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }


    //Getters
    public UUID getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public String getFromAccountId() {
        return this.fromAccountId;
    }

    public String getToAccountId() {
        return this.toAccountId;
    }

    public Money getAmount() {
        return this.amount;
    }

    public String getStatus() {
        return this.status;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }


    // equals & hashCode based on transactionId (unique) – but also include idempotencyKey for safety
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction trans)) return false;
        return id.equals(trans.id) && idempotencyKey.equals(trans.idempotencyKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idempotencyKey);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + id + '\'' +
                ", from=" + fromAccountId +
                ", to=" + toAccountId +
                ", amount=" + amount +
                ", status=" + status +
                ", idempotencyKey='" + idempotencyKey +
                '}';
    }
}