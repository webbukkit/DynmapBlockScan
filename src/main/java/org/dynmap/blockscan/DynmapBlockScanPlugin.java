package org.dynmap.blockscan;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dynmap.blockscan.blockstate.BlockState;
import org.dynmap.blockscan.statehandlers.BedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandlerFactory;
import org.dynmap.blockscan.statehandlers.PistonMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SimpleMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SnowyMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.StairMetadataStateHandler;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import org.dynmap.blockscan.statehandlers.DoorStateHandler;
import org.dynmap.blockscan.statehandlers.RedstoneWireStateHandler;
import org.dynmap.blockscan.statehandlers.NSEWUConnectedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.NSEWConnectedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.GateMetadataStateHandler;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class DynmapBlockScanPlugin
{
    public static OurLog logger = new OurLog();
    public static DynmapBlockScanPlugin plugin;
    private MinecraftServer server;
    
    private IStateHandlerFactory[] state_handler = {
        new NSEWConnectedMetadataStateHandler(),
        new NSEWUConnectedMetadataStateHandler(),
        new RedstoneWireStateHandler(),
        new GateMetadataStateHandler(),
        new StairMetadataStateHandler(),
        new DoorStateHandler(),
        new PistonMetadataStateHandler(),
        new SnowyMetadataStateHandler(),
        new BedMetadataStateHandler(),
        new SimpleMetadataStateHandler()
    };

    public DynmapBlockScanPlugin(MinecraftServer srv)
    {
        plugin = this;
        this.server = srv;
    }


    public void onEnable() {
        logger.info("onEnable()");
    }
    public void onDisable() {
        logger.info("onDisable()");
    }
    public void serverStarted() {
        logger.info("serverStarted()");
        // Scan blocks and block states
        for (Block b : Block.REGISTRY) {
            ResourceLocation rl = b.getRegistryName();
            logger.info(String.format("Block %s: %d", rl, Block.getIdFromBlock(b)));
            BlockStateContainer bsc = b.getBlockState();
            // Check for matching handler
            IStateHandler handler = null;
            for (IStateHandlerFactory f : state_handler) {
                handler = f.canHandleBlockState(b, bsc);
                if (handler != null) {
                    logger.info("  Handled by " + handler.getName());
                    break;
                }
            }
            if (handler == null) {
                logger.info("  NO MATCHING HANDLER");
            }
            Collection<IProperty<?>> props = bsc.getProperties();
            for (IBlockState valid : bsc.getValidStates()) {
                StringBuilder sb = new StringBuilder();
                for(IProperty<?> p : props) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(p.getName()).append("=").append(valid.getValue(p));
                }
                logger.info(String.format("  State %s: meta=%d, rendertype=%s", sb.toString(), b.getMetaFromState(valid), valid.getRenderType()));
            }
            // Try to find blockstate file
            String modid = rl.getResourceDomain();
            String path = "assets/" + modid + "/blockstates/" + rl.getResourcePath() + ".json";
            InputStream is = openResource(modid, path);
            if (is != null) {	// Found it?
            	Reader rdr = new InputStreamReader(is, Charsets.UTF_8);
            	Gson parse = BlockState.buildParser();	// Get parser
            	BlockState bs = parse.fromJson(rdr, BlockState.class);
            	try {
					is.close();
				} catch (IOException e) {
				}
            	if (bs != null) {
            		logger.info(bs.toString());
            	}
            	else {
            		logger.info("Failed to load blockstate!");
            	}
            }
            else {
        		logger.info("Failed to open blockstate " + path + " for modid " + modid);
            }
        }
    }
    
    public InputStream openResource(String modid, String rname) {
        if (modid != null) {
            ModContainer mc = Loader.instance().getIndexedModList().get(modid);
            Object mod = mc.getMod();
            if (mod != null) {
                InputStream is = mod.getClass().getClassLoader().getResourceAsStream(rname);
                if (is != null) {
                    return is;
                }
            }
        }
        List<ModContainer> mcl = Loader.instance().getModList();
        for (ModContainer mc : mcl) {
            Object mod = mc.getMod();
            if (mod == null) continue;
            InputStream is = mod.getClass().getClassLoader().getResourceAsStream(rname);
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    public static class OurLog {
        Logger log;
        public static final String DM = "[DynmapBlockScan] ";
        OurLog() {
            log = LogManager.getLogger("DynmapBlockScan");
        }
        public void info(String s) {
            log.info(DM + s);
        }
        public void severe(Throwable t) {
            log.fatal(t);
        }
        public void severe(String s) {
            log.fatal(DM + s);
        }
        public void severe(String s, Throwable t) {
            log.fatal(DM + s, t);
        }
        public void verboseinfo(String s) {
            log.info(DM + s);
        }
        public void warning(String s) {
            log.warn(DM + s);
        }
        public void warning(String s, Throwable t) {
            log.warn(DM + s, t);
        }
    }
}

