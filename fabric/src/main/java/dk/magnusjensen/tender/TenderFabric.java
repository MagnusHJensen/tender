package dk.magnusjensen.tender;

import dk.magnusjensen.tender.commands.CommandHandler;
import dk.magnusjensen.tender.config.ServerConfig;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.neoforged.fml.config.ModConfig;

public class TenderFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommonClass.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandHandler.register(dispatcher));

        NeoForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        NeoForgeModConfigEvents.reloading(Constants.MOD_ID).register(config -> ServerConfig.onModConfigEvent());
        NeoForgeModConfigEvents.loading(Constants.MOD_ID).register(config -> ServerConfig.onModConfigEvent());

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> CommonClass.stop());

        ServerPlayerEvents.JOIN.register(CommonClass::onPlayerJoin);
    }
}
