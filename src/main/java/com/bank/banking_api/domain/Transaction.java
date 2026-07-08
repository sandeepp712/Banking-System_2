package com.bank.banking_api.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Transaction {
    private final String fromAccountId;
    private final String toAccountId;
    private final Money amount;
    private final String transactionId;
    private final Instant timestamp;
    private final TransactionStatus status;
    private final String idempotencyKey;

    private Transaction(String fromAccountId, String toAccountId, Money amount, String transactionId, Instant timestamp, TransactionStatus status, String idempotencyKey) {
        this.fromAccountId = Objects.requireNonNull(fromAccountId, "fromAccountId is null");
        this.toAccountId = Objects.requireNonNull(toAccountId, "toAccountId is null");
        this.amount = Objects.requireNonNull(amount, "amount is null");
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId is null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is null");
        this.status = Objects.requireNonNull(status, "status is null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey is null");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("fromAccountId and toAccountId can't be the same");
        }

        if (!amount.isPositive()) {
            throw new IllegalArgumentException("amount can't be negative");
        }
    }


    //Getters
    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public Money getAmount() {
        return amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Transaction newStatus(TransactionStatus newStatus) {
        if (this.status == newStatus) {
            return this;
        }
        return new Transaction(
                this.fromAccountId, this.toAccountId,
                this.amount, this.transactionId,
                this.timestamp, newStatus, this.idempotencyKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fromAccountId;
        private String toAccountId;
        private Money amount;
        private String transactionId;
        private Instant timestamp;
        private TransactionStatus status = TransactionStatus.PENDING;
        private String idempotencyKey;

        public Builder fromAccountId(String fromAccountId) {
            this.fromAccountId = fromAccountId;
            return this;
        }

        public Builder toAccountId(String toAccountId) {
            this.toAccountId = toAccountId;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder idempotencyKey(String key) {
            this.idempotencyKey = key;
            return this;
        }

        //Auto generated transactionID
        public Transaction build() {
            if (transactionId == null) transactionId = UUID.randomUUID().toString();
            if (timestamp == null) timestamp = Instant.now();
            return new Transaction(fromAccountId, toAccountId, amount, transactionId, timestamp, status, idempotencyKey);
        }

        // equals & hashCode based on transactionId (unique) – but also include idempotencyKey for safety
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transaction trans)) return false;
            return transactionId.equals(trans.transactionId) && idempotencyKey.equals(trans.idempotencyKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, idempotencyKey);
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "transactionId='" + transactionId + '\'' +
                    ", from=" + fromAccountId +
                    ", to=" + toAccountId +
                    ", amount=" + amount +
                    ", status=" + status +
                    ", idempotencyKey='" + idempotencyKey +
                    '}';
        }
    }
}