package org.dynmap.blockscan.forge_1_16_5;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    public static boolean verboselogging = false;
    
    public DynmapBlockScanMod() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void setup(final FMLCommonSetupEvent event) {
        jarfile = ModList.get().getModFileById("dynmapblockscan").getFile().getFilePath().toFile();

        // Load configuration file - use suggested (config/DynmapBlockScan.cfg)
//        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
//        try {
//            cfg.load();
//            
//           verboselogging = cfg.get("Settings",  "verboselog", false).getBoolean(false);
//        }
//        finally
//        {
//            cfg.save();
//        }
    }

    private MinecraftServer server;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        if(plugin == null)
            plugin = proxy.startServer(server);
        plugin.serverStarting();
    }
    
    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        plugin.serverStarted();
    }
    
    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent event) {
    	proxy.stopServer(plugin);
    	plugin = null;
    }
//    @NetworkCheckHandler
//    public boolean netCheckHandler(Map<String, String> mods, Side side) {
//        return true;
//    }    
}
