package com.bank.banking_api.service;

import com.bank.banking_api.domain.Account;
import com.bank.banking_api.domain.AccountRepository;
import com.bank.banking_api.domain.Money;
import com.bank.banking_api.domain.Transaction;
import com.bank.banking_api.persistence.JdbcTransactionRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TransferService {
    private final AccountRepository accountRepository;
    private final JdbcTransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, JdbcTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, Money amount, String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateKeyException("Transaction id " + idempotencyKey + " already exists.");
        }

        //Idempotency check : stop double spending
        try {

            if (fromAccountId.equals(toAccountId)) {
                throw new IllegalArgumentException("From account id cannot be the same as to account id");
            }

            // Deadlock prevention: lock accounts in a global order (by account number)
            String firstLock = fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId;
            String secondLock = firstLock.equals(fromAccountId) ? toAccountId : fromAccountId;

            // Acquire pessimistic locks (both inside the same transaction)
            Account first = accountRepository.findByAccountNumberForUpdate(firstLock).orElseThrow(() -> new IllegalArgumentException("Account not found" + firstLock));
            Account second = accountRepository.findByAccountNumberForUpdate(secondLock).orElseThrow(() -> new IllegalArgumentException("Account not found" + secondLock));

            // Map locked Accounts to actual from/to
            Account from = first.getAccountNumber().equals(fromAccountId) ? first : second;
            Account to = (from == first) ? second : first;

            //Business logic
            from.debit(amount); //subtract from source
            to.credit(amount);   //add to destination

            // persist the changes - updates are done inside the transaction, locks held until commit
            accountRepository.update(from);
            accountRepository.update(to);

            Transaction tx = new Transaction("TRANSFER", fromAccountId, toAccountId, amount, idempotencyKey);
            transactionRepository.save(tx);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Transaction id " + idempotencyKey + " already exists");
        }
    }
}