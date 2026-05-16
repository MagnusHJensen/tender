package dk.magnusjensen.tender.api;

public sealed interface TransactionResult {
    record Success(long newBalance) implements TransactionResult {}
    record InsufficientFunds(long required, long available) implements TransactionResult {}
    record NotSupported(Capability missing) implements TransactionResult {}
    record Failure(String reason) implements TransactionResult {}
    // TODO: Check if we need a Deferred to indicate a layer result
}
