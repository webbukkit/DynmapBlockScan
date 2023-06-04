package org.dynmapblockscan.forge_1_16_5;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dynmapblockscan.core.AbstractBlockScanBase;
import org.dynmapblockscan.core.BlockScanLog;
import org.dynmapblockscan.core.BlockStateOverrides.BlockStateOverride;
import org.dynmapblockscan.core.blockstate.BSBlockState;
import org.dynmapblockscan.core.blockstate.VariantList;
import org.dynmapblockscan.core.model.BlockModel;
import org.dynmapblockscan.core.statehandlers.StateContainer.StateRec;
import org.dynmapblockscan.forge_1_16_5.statehandlers.ForgeStateContainer;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockReader;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class DynmapBlockScanPlugin extends AbstractBlockScanBase
{
    public static DynmapBlockScanPlugin plugin;
        
    public DynmapBlockScanPlugin(MinecraftServer srv)
    {
        plugin = this;
        logger = new OurLog();
    }

    public void buildAssetMap() {
    	assetmap = new HashMap<String, PathElement>();
        List<ModInfo> mcl = ModList.get().getMods();
        for (ModInfo mc : mcl) {
        	String mid = mc.getModId().toLowerCase();
        	ModFileInfo mfi = mc.getOwningFile();
        	if (mfi == null) continue;
        	ModFile mf = mfi.getFile();
        	if (mf == null) continue;
            try {
            	File src = mf.getFilePath().toFile();
            	// Process mod file
            	processModFile(mid, src);
            }
            catch (UnsupportedOperationException ex) {
            	logger.warning("jar in jar method found, skipping: " + ex.getMessage());
            }
        }
    }
    
    public void onEnable() {
    }
    public void onDisable() {
    }
    public void serverStarted() {
    }
    public void serverStarting() {
    	logger.info("buildAssetMap");
    	buildAssetMap();
    	logger.info("loadOverrideResources");
        // Load override resources
    	loadOverrideResources();
    	logger.info("scan for overrides");
        // Scan other modules for block overrides
        for (ModInfo mod : ModList.get().getMods()) {
        	loadModuleOverrideResources(mod.getModId());
        }

        Map<String, BlockRecord> blockRecords = new HashMap<String, BlockRecord>();

    	logger.info("Start processing states");

        // Now process models from block records
        Map<String, BlockModel> models = new HashMap<String, BlockModel>();

        ObjectIntIdentityMap<net.minecraft.block.BlockState> bsids = Block.BLOCK_STATE_REGISTRY;
        Block baseb = null;
        
        Iterator<net.minecraft.block.BlockState> iter = bsids.iterator();
        // Scan blocks and block states
        while (iter.hasNext()) {
            net.minecraft.block.BlockState blkstate = iter.next();
            Block b = blkstate.getBlock();
            if (b == baseb) { continue; }
            baseb = b;
            ResourceLocation rl = b.getRegistryName();
            //logger.info(String.format("Block %s", rl.toString()));
            net.minecraft.state.StateContainer<Block, net.minecraft.block.BlockState> bsc = b.getStateDefinition();
            // See if any of the block states use MODEL
            boolean uses_model = false;
            boolean uses_nonmodel = false;
            for (net.minecraft.block.BlockState bs : bsc.getPossibleStates()) {
            	switch (bs.getRenderShape()) {
            		case MODEL:
            			uses_model = true;
            			break;
            		case INVISIBLE:
            			uses_nonmodel = true;
            			if (DynmapBlockScanMod.verboselogging)
            			    logger.info(String.format("%s: Invisible block - nothing to render", rl));
            			break;
            		case ENTITYBLOCK_ANIMATED:
            			uses_nonmodel = true;
                        if (DynmapBlockScanMod.verboselogging)
                   			logger.info(String.format("%s: Animated block - needs to be handled specially", rl));
            			break;
//            		case LIQUID:
//            			uses_nonmodel = true;
//                        if (DynmapBlockScanMod.verboselogging)
//                            logger.info(String.format("%s: Liquid block - special handling", rl));
//            			break;
            	}
            }
            // Not model block - nothing else to do yet
            if (!uses_model) {
            	continue;
            }
            else if (uses_nonmodel) {
            	logger.warning(String.format("%s: Block mixes model and nonmodel state handling!", rl));
            }
            // Generate property value map
            Map<String, List<String>> propMap = buildPropoertMap(bsc);
            // Try to find blockstate file
            Material mat = blkstate.getMaterial();
            MaterialColor matcol = mat.getColor();
            BSBlockState blockstate = loadBlockState(rl.getNamespace(), rl.getPath(), overrides, propMap);
            // Build block record
            BlockRecord br = new BlockRecord();
            // Process blockstate
        	if (blockstate != null) {
                br.renderProps = blockstate.getRenderProps();
                br.materialColorID = MaterialColorID.byID(matcol.id);
                br.lightAttenuation = 15;
                try {	// Workaround for mods with broken block state logic...
                	br.lightAttenuation = blkstate.isSolidRender(EmptyBlockReader.INSTANCE, BlockPos.ZERO) ? 15 : (blkstate.propagatesSkylightDown(EmptyBlockReader.INSTANCE, BlockPos.ZERO) ? 0 : 1);
                } catch (Exception x) {
                	logger.warning(String.format("Exception while checking lighting data for block state: %s", blkstate));
                	logger.verboseinfo("Exception: " + x.toString());
                }
        	}
        	// Build generic block state container for block
        	br.sc = new ForgeStateContainer(b, br.renderProps, propMap);
        	if (blockstate != null) {
                BlockStateOverride ovr = overrides.getOverride(rl.getNamespace(), rl.getPath());
            	br.varList = new HashMap<StateRec, List<VariantList>>();
        		// Loop through rendering states in state container
        		for (StateRec sr : br.sc.getValidStates()) {
                    Map<String, String> prop = sr.getProperties();
                    // If we've got key=value for block (multiple blocks in same state file)
                    if ((ovr != null) && (ovr.blockStateKey != null) && (ovr.blockStateValue != null)) {
                        prop = new HashMap<String, String>(prop);
                        prop.put(ovr.blockStateKey, ovr.blockStateValue);
                    }
        			List<VariantList> vlist = blockstate.getMatchingVariants(prop, models);
        			br.varList.put(sr, vlist);
        		}
        	}
        	else {
        	    br.varList = Collections.emptyMap();
        	}
            blockRecords.put(rl.toString(), br);
        }
        
        logger.info("Loading models....");
        loadModels(blockRecords, models);
        logger.info("Variant models loaded");
        // Now, resolve all parent references - load additional models
        resolveParentReferences(models);
        logger.info("Parent models loaded and resolved");
        // Now resolve the elements for all the variants
        resolveAllElements(blockRecords, models);

        logger.info("Elements generated");
        
        publishDynmapModData();
        
        assetmap = null;
    }    
        
    @Override
    public InputStream openResource(String modid, String rname) {
        if (modid.equals("minecraft")) modid = "dynmapblockscan";   // We supply resources (1.13.2 doesn't have assets in server jar)
        String rname_lc = rname.toLowerCase();
        Optional<? extends ModContainer> mc = ModList.get().getModContainerById(modid);
        Object mod = (mc.isPresent())?mc.get().getMod():null;
        ClassLoader cl = MinecraftServer.class.getClassLoader();
        if (mod != null) {
            cl = mod.getClass().getClassLoader();
        }
        if (cl != null) {
            InputStream is = cl.getResourceAsStream(rname_lc);
            if (is == null) {
                is = cl.getResourceAsStream(rname);
            }
            if (is != null) {
                return is;
            }
        }
        return null;
    }
    
    public Map<String, List<String>> buildPropoertMap(net.minecraft.state.StateContainer<Block, net.minecraft.block.BlockState> bsc) {
    	Map<String, List<String>> renderProperties = new HashMap<String, List<String>>();
		// Build table of render properties and valid values
		for (Property<?> p : bsc.getProperties()) {
			String pn = p.getName();
			ArrayList<String> pvals = new ArrayList<String>();
			for (Comparable<?> val : p.getPossibleValues()) {
				if (val instanceof IStringSerializable) {
					pvals.add(((IStringSerializable)val).getSerializedName());
				}
				else {
					pvals.add(val.toString());
				}
			}
			renderProperties.put(pn, pvals);
		}
		return renderProperties;
    }
    
    // Build ImmutableMap<String, String> from properties in BlockState
    public Map<String, String> fromBlockState(net.minecraft.block.BlockState bs) {
    	ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
    	for (Property<?> x : bs.getProperties()) {
    	    Object v = bs.getValue(x);
    		if (v instanceof IStringSerializable) {
    			bld.put(x.getName(), ((IStringSerializable)v).getSerializedName());
    		}
    		else {
    			bld.put(x.getName(), v.toString());
    		}
    	}
    	return bld.build();
    }
    
    public static class OurLog implements BlockScanLog {
        Logger log;
        public static final String DM = "[DynmapBlockScan] ";
        OurLog() {
            log = LogManager.getLogger("DynmapBlockScan");
        }
        public void debug(String s) {
            log.debug(DM + s);
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

