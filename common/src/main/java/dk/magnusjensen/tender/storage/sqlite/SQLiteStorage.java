package dk.magnusjensen.tender.storage.sqlite;

import dk.magnusjensen.tender.api.TransactionResult;
import dk.magnusjensen.tender.storage.TenderStorage;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;

public class SQLiteStorage implements TenderStorage {

    private final Connection connection;
    private final ExecutorService dbExecutor;

    public SQLiteStorage() {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

            Properties props = config.toProperties();
            this.connection = new JDBC().connect("jdbc:sqlite:tender.db", props);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Apply migrations on startup
        Migrations.migrate(connection);

        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sqlite-db-thread");
            t.setDaemon(true);
            return t;
        });
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public CompletableFuture<TransactionResult> deposit(String accountId, long amount) {
        return CompletableFuture.supplyAsync(() -> {
            String select = "SELECT balance FROM tender_fallback_economy WHERE account_id = ?";
            long currentBalance;
            try (var stmt = connection.prepareStatement(select)) {
                stmt.setString(1, accountId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return new TransactionResult.Failure("Account does not exist");
                    }
                    currentBalance = rs.getLong("balance");
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            var query = "UPDATE tender_fallback_economy SET balance = balance + ? WHERE account_id = ?";
            try (var stmt = this.getConnection().prepareStatement(query)) {
                stmt.setLong(1, amount);
                stmt.setString(2, accountId);
                stmt.executeUpdate();
                return new TransactionResult.Success(currentBalance + amount);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<TransactionResult> withdraw(String accountId, long amount) {
        return CompletableFuture.supplyAsync(() -> {
            String select = "SELECT balance FROM tender_fallback_economy WHERE account_id = ?";
            long currentBalance;
            try (var stmt = connection.prepareStatement(select)) {
                stmt.setString(1, accountId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return new TransactionResult.Failure("Account does not exist");
                    }
                    currentBalance = rs.getLong("balance");
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            if (currentBalance < amount) {
                return new TransactionResult.InsufficientFunds(amount, currentBalance);
            }

            var query = "UPDATE tender_fallback_economy SET balance = balance - ? WHERE account_id = ?";
            try (var stmt = this.getConnection().prepareStatement(query)) {
                stmt.setLong(1, amount);
                stmt.setString(2, accountId);
                stmt.executeUpdate();
                return new TransactionResult.Success(currentBalance - amount);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<TransactionResult> transfer(String fromAccountId, String toAccountId, long amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean previousAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    // Validate source account and balance
                    long fromBalance;
                    String fromQuery = "SELECT balance FROM tender_fallback_economy WHERE account_id = ?";
                    try (var stmt = connection.prepareStatement(fromQuery)) {
                        stmt.setString(1, fromAccountId);
                        try (var rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                connection.rollback();
                                return new TransactionResult.Failure("Source account does not exist");
                            }
                            fromBalance = rs.getLong("balance");
                        }
                    }

                    // Validate destination account
                    String toQuery = "SELECT 1 FROM tender_fallback_economy WHERE account_id = ?";
                    try (var stmt = connection.prepareStatement(toQuery)) {
                        stmt.setString(1, toAccountId);
                        try (var rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                connection.rollback();
                                return new TransactionResult.Failure("Destination account does not exist");
                            }
                        }
                    }

                    if (fromBalance < amount) {
                        connection.rollback();
                        return new TransactionResult.InsufficientFunds(amount, fromBalance);
                    }

                    // Debit source
                    String debit = "UPDATE tender_fallback_economy SET balance = balance - ? WHERE account_id = ?";
                    try (var stmt = connection.prepareStatement(debit)) {
                        stmt.setLong(1, amount);
                        stmt.setString(2, fromAccountId);
                        stmt.executeUpdate();
                    }

                    // Credit destination
                    String credit = "UPDATE tender_fallback_economy SET balance = balance + ? WHERE account_id = ?";
                    try (var stmt = connection.prepareStatement(credit)) {
                        stmt.setLong(1, amount);
                        stmt.setString(2, toAccountId);
                        stmt.executeUpdate();
                    }

                    connection.commit();
                    return new TransactionResult.Success(amount);
                } catch (SQLException e) {
                    connection.rollback();
                    throw new CompletionException(e);
                } finally {
                    connection.setAutoCommit(previousAutoCommit);
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Optional<Long>> getBalance(String accountId) {
        return CompletableFuture.supplyAsync(() -> {
            var query = "SELECT balance FROM tender_fallback_economy WHERE account_id = ?";
            try (var stmt = this.getConnection().prepareStatement(query)) {
                stmt.setString(1, accountId);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(rs.getLong("balance"));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbExecutor);
    }

    @Override
    public void createAccount(String accountId) {
        CompletableFuture.runAsync(() -> {
            var query = "INSERT INTO tender_fallback_economy (account_id, balance) VALUES (?, 0) ON CONFLICT(account_id) DO NOTHING";
            try (var stmt = this.getConnection().prepareStatement(query)) {
                stmt.setString(1, accountId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbExecutor);
    }

    @Override
    public void stop() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
