package com.bank.banking_api.service;

import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.Money;
import com.bank.banking_api.persistence.JdbcAccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service              // spring manage this bean(object)
public class AccountService {
    private final JdbcAccountRepository jdbcAccountRepository;


    //Spring will automatically inject the JdbcAccountRepository here!
    public AccountService(JdbcAccountRepository jdbcAccountRepository) {
        this.jdbcAccountRepository = jdbcAccountRepository;
    }

    /**
     * Creates a new account and saves it to the repository.
     */
    public Account createAccount(String accountNo, Money initialBalance) {
        if (jdbcAccountRepository.findByAccountNumber(accountNo).isPresent()) {
            throw new IllegalArgumentException("Account already exists : " + accountNo);
        }

        Account account = new Account(accountNo, initialBalance);
        jdbcAccountRepository.save(account);

        return account;
    }


    /**
     * To get the particular account is present or not
     *
     * @param id
     * @return
     */
    public Account getAccount(String accountNumber) {
        return jdbcAccountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new IllegalArgumentException("Account not found : " + accountNumber));
    }

    /**
     * Deposits money into an account.
     *
     * @return The Transaction record representing this deposit.
     */
    public Account deposit(String accountNumber, Money amount) {
        Account account = getAccount(accountNumber);

        account.credit(amount);
        jdbcAccountRepository.update(account);
        return account;
    }


    public Account withdraw(String accountNumber, Money amount) {
        Account account = getAccount(accountNumber);
        account.debit(amount);
        jdbcAccountRepository.update(account);
        return account;
    }


}