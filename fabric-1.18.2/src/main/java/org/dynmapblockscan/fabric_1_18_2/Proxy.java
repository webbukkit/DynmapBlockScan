package org.dynmapblockscan.fabric_1_18_2;

import net.minecraft.server.MinecraftServer;

/**
 * Server side proxy - methods for creating and cleaning up plugin
 */
public class Proxy
{
    public Proxy()
    {
    }
	public DynmapBlockScanPlugin startServer(MinecraftServer srv) {
	    DynmapBlockScanPlugin plugin = DynmapBlockScanPlugin.plugin;
	    if (plugin == null) {
	        plugin = new DynmapBlockScanPlugin(srv);
	        plugin.onEnable();
	    }
		return plugin;
	}
	public void stopServer(DynmapBlockScanPlugin plugin) {
		plugin.onDisable();
	}
}
