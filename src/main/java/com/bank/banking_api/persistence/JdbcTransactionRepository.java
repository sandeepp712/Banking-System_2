package com.bank.banking_api.persistence;

import com.bank.banking_api.domain.Money;
import com.bank.banking_api.domain.Transaction;
import com.bank.banking_api.domain.TransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Repository
public class JdbcTransactionRepository implements TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        UUID id = rs.getObject("id", UUID.class);
        String type = rs.getString("type");
        String fromAccount = rs.getString("fromAccount");
        String toAccount = rs.getString("toAccount");
        BigDecimal amount = rs.getBigDecimal("amount");
        String currency = rs.getString("currency");
        String status = rs.getString("status");
        String idempotencyKey = rs.getString("idempotencyKey");
        Instant created_at = rs.getTimestamp("created_at").toInstant();

        Money money = Money.of(amount, Currency.getInstance(currency));

        return new Transaction(type, fromAccount, toAccount, money, idempotencyKey);
    };

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Transaction transaction) {
        String sql = """
                Insert into transactions (id, type,from_account,to_account ,amount,currency,status,idempotency_key,created_at)
                values (?,?,?,?,?,?,?,?,?)
                """;

        jdbcTemplate.update(sql,
                transaction.getId(),
                transaction.getType(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount().getAmount(),
                transaction.getAmount().getCurrency().getCurrencyCode(),
                transaction.getStatus(),
                transaction.getIdempotencyKey(),
                java.sql.Timestamp.from(transaction.getCreatedAt())
        );
    }

    //Crucial for Idempotency!
    public boolean existsByIdempotencyKey(String key) {
        String sql = "Select count(*) from transactions where idempotency_key=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, key);
        return count != null && count > 0;
    }
}