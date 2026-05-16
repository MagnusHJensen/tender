package dk.magnusjensen.tender.config;

import net.neoforged.neoforge.common.ModConfigSpec;


public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> overrideProviderPriority = BUILDER
            .comment("Override the provider priority. Provide a single value, for a valid economy provider id. Can be found if loaded via /tender info")
            .define("overrideProviderPriority", "");
    private static final ModConfigSpec.ConfigValue<Boolean> registerCommands = BUILDER
        .comment("Whether to register the /tender admin commands to give, take and transfer. Set to false if you want to use the API without exposing any commands to players (other than info).")
        .define("registerCommands", false);

    public static final ModConfigSpec SPEC = BUILDER.build();


    public static String OVERRIDE_PROVIDER_PRIORITY;
    public static boolean REGISTER_COMMANDS;

    public static void onModConfigEvent() {
        OVERRIDE_PROVIDER_PRIORITY = overrideProviderPriority.get();
        REGISTER_COMMANDS = registerCommands.get();
    }
}
