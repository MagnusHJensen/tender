package dk.magnusjensen.tender.fallback;

import dk.magnusjensen.tender.CommonClass;
import dk.magnusjensen.tender.Constants;
import dk.magnusjensen.tender.api.Capability;
import dk.magnusjensen.tender.api.EconomyProvider;
import dk.magnusjensen.tender.api.TransactionResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FallbackProvider implements EconomyProvider {
    @Override
    public String id() {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "fallback").toString();
    }

    @Override
    public String displayName() {
        return "Fallback Economy Provider";
    }

    @Override
    public int priority() {
        return -1000;
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.OFFLINE_DEPOSIT, Capability.OFFLINE_WITHDRAW);
    }

    @Override
    public CompletableFuture<TransactionResult> deposit(@NotNull String accountId, long amount, @Nullable String reason) {
        if (amount < 1) {
            return CompletableFuture.completedFuture(new TransactionResult.Failure("Amount must be positive"));
        }
        return CommonClass.STORAGE.deposit(accountId, amount);
    }

    @Override
    public CompletableFuture<TransactionResult> withdraw(@NotNull String accountId, long amount, @Nullable String reason) {
        if (amount < 1) {
            return CompletableFuture.completedFuture(new TransactionResult.Failure("Amount must be positive"));
        }

        return CommonClass.STORAGE.withdraw(accountId, amount);
    }

    @Override
    public CompletableFuture<TransactionResult> transfer(@NotNull String fromAccountId, @NotNull String toAccountId, long amount, @Nullable String reason) {
        if (amount < 1) {
            return CompletableFuture.completedFuture(new TransactionResult.Failure("Amount must be positive"));
        }

        return CommonClass.STORAGE.transfer(fromAccountId, toAccountId, amount);
    }

    @Override
    public CompletableFuture<Optional<Long>> balance(String accountId) {
        return CommonClass.STORAGE.getBalance(accountId);
    }

    @Override
    public CompletableFuture<Optional<Boolean>> hasAccount(String accountId) {
        // For the fallback provider, we use a simplified approach to check the account
        // Writing another query would provide the same and be as efficient as the getBalance query.
        return CommonClass.STORAGE.getBalance(accountId).thenApply(aLong -> Optional.of(aLong.isPresent()));
    }
}
