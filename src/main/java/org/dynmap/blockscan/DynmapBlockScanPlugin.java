package org.dynmap.blockscan;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dynmap.blockscan.BlockStateOverrides.BlockStateOverride;
import org.dynmap.blockscan.blockstate.BaseCondition;
import org.dynmap.blockscan.blockstate.BlockState;
import org.dynmap.blockscan.blockstate.Multipart;
import org.dynmap.blockscan.blockstate.VariantList;
import org.dynmap.blockscan.statehandlers.BedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandlerFactory;
import org.dynmap.blockscan.statehandlers.PistonMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SimpleMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SnowyMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.StairMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.StateContainer;
import org.dynmap.blockscan.statehandlers.StateContainer.StateRec;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import jline.internal.Log;

import org.dynmap.blockscan.statehandlers.DoorStateHandler;
import org.dynmap.blockscan.statehandlers.ForgeStateContainer;
import org.dynmap.blockscan.statehandlers.RedstoneWireStateHandler;
import org.dynmap.blockscan.statehandlers.NSEWUConnectedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.NSEWConnectedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.GateMetadataStateHandler;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class DynmapBlockScanPlugin
{
    public static OurLog logger = new OurLog();
    public static DynmapBlockScanPlugin plugin;
    
    public static class BlockRecord {
    	public StateContainer sc;
    	public Map<StateRec, List<VariantList>> varList;	// Model references for block
    	public Set<String> renderProps;	// Set of render properties
    	public IStateHandler handler;		// Best handler
    }
    
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
    }


    public void onEnable() {
        logger.info("onEnable()");
    }
    public void onDisable() {
        logger.info("onDisable()");
    }
    public void serverStarted() {
        logger.info("serverStarted()");
        
        // Load override resources
        InputStream override_str = openResource("dynmapblockscan", "blockStateOverrides.json");
        BlockStateOverrides overrides;
        if (override_str != null) {
        	Reader rdr = new InputStreamReader(override_str, Charsets.UTF_8);
        	Gson parse = new Gson();
        	overrides = parse.fromJson(rdr, BlockStateOverrides.class);
        	try {
				override_str.close();
			} catch (IOException e) {
			}
        }
        else {
        	logger.info("Failed to load block overrides");
        	overrides = new BlockStateOverrides();
        }

        Map<String, BlockRecord> blockRecords = new HashMap<String, BlockRecord>();
        // Scan blocks and block states
        for (Block b : Block.REGISTRY) {
            ResourceLocation rl = b.getRegistryName();
            logger.info(String.format("Block %s: %d", rl, Block.getIdFromBlock(b)));
            BlockStateContainer bsc = b.getBlockState();
            // Generate property value map
            Map<String, List<String>> propMap = buildPropoertMap(bsc);
            // Try to find blockstate file
            BlockState blockstate = loadBlockState(rl.getResourceDomain(), rl.getResourcePath(), overrides, propMap);
            // Build block record
            BlockRecord br = new BlockRecord();
            // Process blockstate
        	if (blockstate != null) {
                br.renderProps = blockstate.getRenderProps();
        	}
        	// Build generic block state container for block
        	br.sc = new ForgeStateContainer(b, br.renderProps, propMap);
        	if (blockstate != null) {
            	br.varList = new HashMap<StateRec, List<VariantList>>();
        		// Loop through rendering states in state container
        		for (StateRec sr : br.sc.getValidStates()) {
        			List<VariantList> vlist = blockstate.getMatchingVariants(sr.getProperties());
        			br.varList.put(sr, vlist);
        		}
        	}
            // Check for matching handler
            br.handler = null;
            for (IStateHandlerFactory f : state_handler) {
              	br.handler = f.canHandleBlockState(br.sc);
                if (br.handler != null) {
                    break;
                }
            }
            if (br.handler == null) {
                logger.info(rl + ":  NO MATCHING HANDLER");
            }
            blockRecords.put(rl.toString(), br);
            
            //Collection<IProperty<?>> props = bsc.getProperties();
            //for (IBlockState valid : bsc.getValidStates()) {
            //    StringBuilder sb = new StringBuilder();
            //    for(IProperty<?> p : props) {
            //        if (sb.length() > 0)
            //            sb.append(",");
            //        sb.append(p.getName()).append("=").append(valid.getValue(p));
            //    }
            //    logger.info(String.format("  State %s: meta=%d, rendertype=%s", sb.toString(), b.getMetaFromState(valid), valid.getRenderType()));
            //}
        }
    }
    
    public static InputStream openResource(String modid, String rname) {
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
    
    public Map<String, List<String>> buildPropoertMap(BlockStateContainer bsc) {
    	Map<String, List<String>> renderProperties = new HashMap<String, List<String>>();
		// Build table of render properties and valid values
		for (IProperty<?> p : bsc.getProperties()) {
			String pn = p.getName();
			ArrayList<String> pvals = new ArrayList<String>();
			for (Comparable<?> val : p.getAllowedValues()) {
				if (val instanceof IStringSerializable) {
					pvals.add(((IStringSerializable)val).getName());
				}
				else {
					pvals.add(val.toString());
				}
			}
			renderProperties.put(pn, pvals);
		}
		return renderProperties;
    }
    
    // Build ImmutableMap<String, String> from properties in IBlockState
    public ImmutableMap<String, String> fromIBlockState(IBlockState bs) {
    	ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
    	for (Entry<IProperty<?>, Comparable<?>> x : bs.getProperties().entrySet()) {
    		Comparable<?> v = x.getValue();
    		if (v instanceof IStringSerializable) {
    			bld.put(x.getKey().getName(), ((IStringSerializable)v).getName());
    		}
    		else {
    			bld.put(x.getKey().getName(), v.toString());
    		}
    	}
    	return bld.build();
    }
    
    private static BlockState loadBlockState(String modid, String respath, BlockStateOverrides override, Map<String, List<String>> propMap) {
    	BlockStateOverride ovr = override.getOverride(modid, respath);
    	
    	if (ovr == null) {	// No override
    		return loadBlockStateFile(modid, respath);
    	}
    	else if (ovr.blockStateName != null) {	// Simple override
    		return loadBlockStateFile(modid, ovr.blockStateName);
    	}
    	else if (ovr.baseNameProperty != null) {	// MUltiple files based on base property
    		List<String> vals = propMap.get(ovr.baseNameProperty);	// Look up defned values
    		if (vals == null) {
    			Log.error(String.format("%s:%s : bad baseNameProperty=%s",  modid, respath, ovr.baseNameProperty));;
    			return null;
    		}
    		BlockState bs = new BlockState();
    		bs.nestedProp = ovr.baseNameProperty;
    		bs.nestedValueMap = new HashMap<String, BlockState>();
    		for (String v : vals) {
    			BlockState bs2 = loadBlockStateFile(modid, v + ovr.nameSuffix);
    			if (bs2 != null) {
    				bs.nestedValueMap.put(v,  bs2);
    			}
    		}
    		return bs;
    	}

		return null;
    }
    
    private static BlockState loadBlockStateFile(String modid, String respath) {
        String path = "assets/" + modid + "/blockstates/" + respath + ".json";
    	BlockState bs = null;
        InputStream is = openResource(modid, path);
        if (is != null) {	// Found it?
        	Reader rdr = new InputStreamReader(is, Charsets.UTF_8);
        	Gson parse = BlockState.buildParser();	// Get parser
        	bs = parse.fromJson(rdr, BlockState.class);
        	try {
				is.close();
			} catch (IOException e) {
			}
        	if (bs == null) {
        		logger.info(String.format("%s:%s : Failed to load blockstate!", modid, path));
        	}
        }
        else {
    		logger.info(String.format("%s:%s : Failed to open blockstate", modid, path));
        }
        return bs;
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

