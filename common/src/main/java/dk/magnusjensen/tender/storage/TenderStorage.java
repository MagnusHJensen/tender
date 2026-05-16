package dk.magnusjensen.tender.storage;

import dk.magnusjensen.tender.api.TransactionResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TenderStorage {
    CompletableFuture<TransactionResult> deposit(String accountId, long amount);
    CompletableFuture<TransactionResult> withdraw(String accountId, long amount);
    CompletableFuture<TransactionResult> transfer(String fromAccountId, String toAccountId, long amount);

    CompletableFuture<Optional<Long>> getBalance(String accountId);
    void createAccount(String accountId);

    void stop();
}
