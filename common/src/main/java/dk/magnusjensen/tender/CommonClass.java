package dk.magnusjensen.tender;

import dk.magnusjensen.tender.api.Tender;
import dk.magnusjensen.tender.storage.TenderStorage;
import dk.magnusjensen.tender.storage.sqlite.SQLiteStorage;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;


public class CommonClass {

    // We store our storage instance here, since it's purely for our fallback provider, and not part of the API contract layer.
    public static TenderStorage STORAGE;

    public static void init() {
        // TODO: Do we need access to this in our own core?
        TenderAPIImpl impl = new TenderAPIImpl();
        Tender._init(impl);

        STORAGE = new SQLiteStorage();
    }

    public static void stop() {
        STORAGE.stop();
    }

    public static void onPlayerJoin(ServerPlayer joinedPlayer) {
        String accountId = joinedPlayer.getStringUUID();
        STORAGE.getBalance(accountId)
            .thenCompose(balanceOpt -> {
               if (balanceOpt.isPresent()) {
                   return CompletableFuture.completedFuture(null);
               }

               return CompletableFuture.runAsync(() -> {
                   Constants.LOG.debug("Creating account for player {} with ID {}", joinedPlayer.getName().getString(), accountId);
                   STORAGE.createAccount(accountId);
               });
            })
            .exceptionally(ex -> {
                Constants.LOG.error("Failed to create account for player {}: {}", joinedPlayer.getName().getString(), ex.getMessage(), ex);
                return null;
            });
    }
}