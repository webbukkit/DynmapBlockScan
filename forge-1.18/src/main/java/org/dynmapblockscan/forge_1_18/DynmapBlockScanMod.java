package org.dynmapblockscan.forge_1_18;

import java.io.File;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        if(plugin == null)
            plugin = proxy.startServer(server);
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
