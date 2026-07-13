package com.bank.banking_api.domain;


import com.bank.banking_api.service.AccountService;
import com.bank.banking_api.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferServiceTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    private final Currency INR = Currency.getInstance("INR");

    @BeforeEach
    void setUp() {
        accountService.createAccount("ACC-1", Money.of(new BigDecimal("1000.00"), INR));
        accountService.createAccount("ACC-2", Money.of(new BigDecimal("500.00"), INR));
    }

    @Test
    @DisplayName("Should transfer money from a to b")
    void transferMoneyFromAToB() {
        transferService.transfer("ACC-1", "ACC-2", Money.of(new BigDecimal("100.00"), INR), UUID.randomUUID().toString());
        Account acc1 = accountService.getAccount("ACC-1");
        Account acc2 = accountService.getAccount("ACC-2");

        assertEquals(new BigDecimal("900.00"), acc1.getBalance().getAmount());
        assertEquals(new BigDecimal("600.00"), acc2.getBalance().getAmount());
    }

    @Test
    @DisplayName("Should throw error for insufficient funds")
    void transferMoneyFromInsufficientFunds() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer("ACC-1","ACC-2", Money.of(new BigDecimal("1001.00"), INR), UUID.randomUUID().toString());
        });
    }

    @Test
    @DisplayName("Should be perform concurreny transfer properly")
    void testConcurrentTransfersDeadlock() throws Exception {
        int thread=2;

        ExecutorService executor = Executors.newFixedThreadPool(thread);
        CountDownLatch latch = new CountDownLatch(thread);

        // Two thread: one transfer ACC-1 -> ACC-2, the other ACC-2 -> ACC-1
        Runnable task1 = () -> {
            try {
                transferService.transfer("ACC-1","ACC-2",Money.of(new BigDecimal("100.00"), INR), UUID.randomUUID().toString());
            }finally {
                latch.countDown();
            }
        };

        Runnable task2 = () -> {
            try{
                transferService.transfer("ACC-2","ACC-1",Money.of(new BigDecimal("50.00"), INR), UUID.randomUUID().toString());
            }finally {
                latch.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);
        latch.await(10, TimeUnit.SECONDS);

        //Final balance must be consistent total=1000+500=1500
        Account acc1 = accountService.getAccount("ACC-1");
        Account acc2 = accountService.getAccount("ACC-2");

        BigDecimal total=acc1.getBalance().getAmount().add(acc2.getBalance().getAmount());

        assertEquals(new BigDecimal("1500.00"), total);
        executor.shutdown();
    }

    @Test
    @DisplayName("test should be fail in concurrent transfer")
    void testConcurrentRollbackonInsufficientFunds() {
        String key=UUID.randomUUID().toString();

        BigDecimal amount=new BigDecimal("1000.00");

        // 1. Execute transfer – must fail with an unchecked exception
        assertThrows(IllegalArgumentException.class, () ->
            transferService.transfer("ACC-2","ACC-1",Money.of(amount,INR), UUID.randomUUID().toString())
        );


        // 2. Read the accounts AFTER the failed transaction – this will start a NEW transaction
        //    (because accountService.getAccount() is not @Transactional itself,  I had to comment out @Transactional annotation
        //     each call runs in its own short-lived transaction)
        Account acc1 = accountService.getAccount("ACC-1");
        Account acc2 = accountService.getAccount("ACC-2");

        // 3. Assert that balances are unchanged (rollback succeeded)
        assertEquals(new BigDecimal("1000.00"), acc1.getBalance().getAmount(),
                "ACC-1 balance should remain 1000.00 after failed transfer");
        assertEquals(new BigDecimal("500.00"), acc2.getBalance().getAmount(),
                "ACC-2 balance should remain 500.00 after failed transfer");


    }

    @Test
    void testConcurrentTransfersWithoutTransactional_shouldBeInconsistent() throws Exception {
        // This test assumes the service method does NOT have @Transactional
        // (so the locks are immediately released after each SELECT FOR UPDATE).

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        CyclicBarrier barrier = new CyclicBarrier(2); // makes both threads start at the same time

        // Thread 1: ACC-1 -> ACC-2, amount 100
        executor.submit(() -> {
            try {
                barrier.await();
                transferService.transfer("ACC-1", "ACC-2", Money.of(new BigDecimal("100.00"), INR),
                        UUID.randomUUID().toString());
            } catch (Exception e) {
                // ignore exception
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: ACC-2 -> ACC-1, amount 50
        executor.submit(() -> {
            try {
                barrier.await();
                transferService.transfer("ACC-2", "ACC-1", Money.of(new BigDecimal("50.00"), INR),
                        UUID.randomUUID().toString());
            } catch (Exception e) {
                // ignore exception
            } finally {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Read the final balances (outside of any transaction)
        Account acc1 = accountService.getAccount("ACC-1");
        Account acc2 = accountService.getAccount("ACC-2");
        BigDecimal total = acc1.getBalance().getAmount().add(acc2.getBalance().getAmount());

        // Without @Transactional, the total may not be 1500 (the invariant can be violated)
        // If you run this with @Transactional on transfer(), the total will always be 1500.
        // When you comment out @Transactional, you might sometimes see 1450 or 1550.
        System.out.println("Total after concurrent transfers: " + total);
        assertEquals(new BigDecimal("1500.00"), total, "Money conservation violated");
    }
}