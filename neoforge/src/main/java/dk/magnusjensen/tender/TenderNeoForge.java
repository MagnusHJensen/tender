package dk.magnusjensen.tender;


import dk.magnusjensen.tender.config.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class TenderNeoForge {

    public TenderNeoForge(IEventBus eventBus, ModContainer modContainer) {
        CommonClass.init();

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    @SubscribeEvent
    public static void onConfigUpdates(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) {
            return; // No-op for unloading to avoid exception
        }

        ServerConfig.onModConfigEvent();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        CommonClass.stop();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CommonClass.onPlayerJoin(serverPlayer);
    }
}