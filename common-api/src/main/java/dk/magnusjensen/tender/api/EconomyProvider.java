package dk.magnusjensen.tender.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface EconomyProvider {
    /**
     *
     * @return
     */
    String id();

    /**
     *
     * @return
     */
    String displayName();

    /**
     *
     * @return
     */
    int priority();

    /**
     *
     * @return
     */
    Set<Capability> capabilities();


    /**
     *
     * @param accountId The id of the account to deposit the amount into.
     * @param amount The amount to deposit, in the smallest unit of the currency.
     * @param reason An optional reason for the deposit, which may be used for logging or auditing purposes. It can be null if no reason is provided.
     * @return A TransactionResult indicating the result of the transaction.
     */
    CompletableFuture<TransactionResult> deposit(@NotNull String accountId, long amount, @Nullable String reason);

    /**
     *
     * @param accountId The id of the account to withdraw the amount from.
     * @param amount The amount to withdraw, in the smallest unit of the currency.
     * @param reason An optional reason for the withdraw, which may be used for logging or auditing purposes. It can be null if no reason is provided.
     * @return A TransactionResult indicating the result of the transaction.
     */
    CompletableFuture<TransactionResult> withdraw(@NotNull String accountId, long amount, @Nullable String reason);

    /**
     * Transfers the specified amount from one account to another. They must both exist.
     * @param fromAccountId The id of the account to transfer the amount from.
     * @param toAccountId The id of the account to transfer the amount to.
     * @param amount The amount to transfer, in the smallest unit of the currency.
     * @param reason An optional reason for the transfer, which may be used for logging or auditing purposes. It can be null if no reason is provided.
     * @return A TransactionResult indicating the result of the transaction.
     * If the fromAccountId does not have enough funds it should return a TransactionResult.InsufficientFunds.
     */
    CompletableFuture<TransactionResult> transfer(@NotNull String fromAccountId, @NotNull String toAccountId, long amount, @Nullable String reason);

    /**
     *
     * @param accountId The id of the account to check the balance of.
     * @return An optional long, which is empty if the account does not exist.
     */
    CompletableFuture<Optional<Long>> balance(String accountId);

    /**
     *
     * @param accountId The id of the account to check for existence.
     * @return An optional boolean, which is empty if the account does not exist.
     */
    CompletableFuture<Optional<Boolean>> hasAccount(String accountId);

}
