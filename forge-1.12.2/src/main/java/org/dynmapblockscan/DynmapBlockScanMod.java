package org.dynmapblockscan;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "dynmapblockscan", name = "DynmapBlockScan", version = Version.VER)
public class DynmapBlockScanMod
{
    public static DynmapBlockScanPlugin.OurLog logger = new DynmapBlockScanPlugin.OurLog();
    // The instance of your mod that Forge uses.
    @Instance("dynmapblockscan")
    public static DynmapBlockScanMod instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "org.dynmapblockscan.ClientProxy", serverSide = "org.dynmapblockscan.Proxy")
    public static Proxy proxy;
    
    public static DynmapBlockScanPlugin plugin;
    public static File jarfile;
    public static boolean verboselogging = false;
    public static String[] excludeModules = { "minecraft" };
    public static String[] excludeBlockNames = { };
    
    public DynmapBlockScanMod() {
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        jarfile = event.getSourceFile();
        // Load configuration file - use suggested (config/DynmapBlockScan.cfg)
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();
            
           verboselogging = cfg.get("Settings",  "verboselog", false).getBoolean(false);
           
           excludeModules = cfg.get("Settings", "exclude_modules", new String[] { "minecraft" }).getStringList();
           excludeBlockNames = cfg.get("Settings", "exclude_blocknames", new String[0]).getStringList();

        }
        finally
        {
            cfg.save();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    private MinecraftServer server;
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        if(plugin == null)
            plugin = proxy.startServer(server);
        plugin.setDisabledModules(Arrays.asList(excludeModules));
        plugin.setDisabledBlockNames(Arrays.asList(excludeBlockNames));
        plugin.serverStarting();
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        plugin.serverStarted();
    }
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
    	proxy.stopServer(plugin);
    	plugin = null;
    }
    @NetworkCheckHandler
    public boolean netCheckHandler(Map<String, String> mods, Side side) {
        return true;
    }    
}
