package org.dynmapblockscan.forge_1_20;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;

@Mod("dynmapblockscan")
public class DynmapBlockScanMod
{
    public static DynmapBlockScanPlugin.OurLog logger = new DynmapBlockScanPlugin.OurLog();
    // The instance of your mod that Forge uses.
    public static DynmapBlockScanMod instance;

    // Says where the client and server 'proxy' code is loaded.
    public static Proxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> Proxy::new);

    public static DynmapBlockScanPlugin plugin;
    public static File jarfile;
    
    public DynmapBlockScanMod() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);

        FMLPaths.getOrCreateGameRelativePath(Path.of("config", "dynmapblockscan"));
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SettingsConfig.SPEC, "dynmapblockscan/settings.toml");
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

    public void setup(final FMLCommonSetupEvent event) {
    	logger.info("setup");
        jarfile = ModList.get().getModFileById("dynmapblockscan").getFile().getFilePath().toFile();
    }

    private MinecraftServer server;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        if(plugin == null)
            plugin = proxy.startServer(server);
        plugin.setDisabledModules((List<String>) SettingsConfig.excludeModules.get());
        plugin.setDisabledBlockNames((List<String>) SettingsConfig.excludeBlockNames.get());
        plugin.serverStarting();
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        plugin.serverStarted();
    }
    
    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
    	proxy.stopServer(plugin);
    	plugin = null;
    }
//    @NetworkCheckHandler
//    public boolean netCheckHandler(Map<String, String> mods, Side side) {
//        return true;
//    }    
}
