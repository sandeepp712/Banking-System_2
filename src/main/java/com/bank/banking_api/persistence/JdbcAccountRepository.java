package com.bank.banking_api.persistence;

import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.Money;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAccountRepository {
    private JdbcTemplate jdbcTemplate;

    public RowMapper<Account> rowMapper = (rs, rowNum) -> {
        UUID id = rs.getObject("id", UUID.class);
        String accountNumber = rs.getString("account_number");
        BigDecimal amount = rs.getBigDecimal("balance_amount");
        String currency = rs.getString("balance_currency");
        String status = rs.getString("status");
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        Instant updatedAt = rs.getTimestamp("updated_at").toInstant();

        Money balance = Money.of(amount, Currency.getInstance(currency));

        return new Account(id, accountNumber, balance, status, createdAt, updatedAt);
    };

    public JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Account accounts) {
        String sql = """
                INSERT INTO accounts (id, account_number, balance_amount, balance_currency, status, created_at, updated_at) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                accounts.getId(),
                accounts.getAccountNumber(),
                accounts.getBalance().getAmount(),      // Unpack Money to BigDecimal
                accounts.getBalance().getCurrency().getCurrencyCode(),    //  Unpack Money to String
                accounts.getStatus(),
                java.sql.Timestamp.from(accounts.getCreatedAt()),
                java.sql.Timestamp.from(accounts.getUpdatedAt())

        );
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "Select * from accounts where account_number = ?";
        return jdbcTemplate.query(sql, rowMapper, accountNumber).stream().findFirst();
    }


    public void update(Account accounts) {
        String sql = "Update accounts SET balance_amount=?,updated_at=? where account_number=?";
        jdbcTemplate.update(sql, accounts.getBalance().getAmount(), java.sql.Timestamp.from(accounts.getUpdatedAt()), accounts.getAccountNumber());
    }
}