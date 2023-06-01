package org.dynmapblockscan.fabric_1_18_2;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DynmapBlockScanMod implements ModInitializer
{
    public static DynmapBlockScanPlugin.OurLog logger = new DynmapBlockScanPlugin.OurLog();
    private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("dynmapblockscan")
            .orElseThrow(() -> new RuntimeException("Failed to get mod container: dynmapblockscan"));

    // The instance of your mod that Forge uses.
    public static DynmapBlockScanMod instance;

    // Says where the client and server 'proxy' code is loaded.
    public static Proxy proxy = null;

    public static DynmapBlockScanPlugin plugin;
    public static File jarfile;

    public DynmapBlockScanMod(){
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            proxy = new ClientProxy();
        }else{
            proxy = new Proxy();
        }
    }
    @Override
    public void onInitialize(){
        instance = this;

        logger.info("setup");

        jarfile = MOD_CONTAINER.getOrigin().getPaths().get(0).toFile();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);

        FileUtils.getOrCreateDirectory(FabricLoader.getInstance().getConfigDir().resolve("dynmapblockscan"), "dynmapblockscan");


        ModLoadingContext.registerConfig("dynmapblockscan", ModConfig.Type.COMMON, SettingsConfig.SPEC, "dynmapblockscan/settings.toml");

    }

    public static class SettingsConfig
    {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> excludeModules;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> excludeBlockNames;

        static
        {
            BUILDER.comment("DynmapBlockScan settings");
            BUILDER.push("settings");
            excludeModules = BUILDER.comment("Which modules to exclude").defineList("exclude_modules", Arrays.asList("minecraft"), entry -> true);
            excludeBlockNames = BUILDER.comment("Which block names to exclude").defineList("exclude_blocknames", Arrays.asList(), entry -> true);
            BUILDER.pop();

            SPEC = BUILDER.build();
        }
    }

    private MinecraftServer server;

    public void onServerStarting(MinecraftServer server_) {
        server = server_;
        if(plugin == null)
            plugin = proxy.startServer(server);
        plugin.setDisabledModules((List<String>) SettingsConfig.excludeModules.get());
        plugin.setDisabledBlockNames((List<String>) SettingsConfig.excludeBlockNames.get());
        plugin.serverStarting();
    }

    public void onServerStarted(MinecraftServer server_) {
        plugin.serverStarted();
    }

    public void onServerStopped(MinecraftServer server_) {
        proxy.stopServer(plugin);
        plugin = null;
    }

//    @NetworkCheckHandler
//    public boolean netCheckHandler(Map<String, String> mods, Side side) {
//        return true;
//    }    
}
