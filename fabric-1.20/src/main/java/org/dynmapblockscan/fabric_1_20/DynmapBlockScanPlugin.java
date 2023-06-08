package org.dynmapblockscan.fabric_1_20;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.block.MapColor;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dynmapblockscan.core.AbstractBlockScanBase;
import org.dynmapblockscan.core.BlockScanLog;
import org.dynmapblockscan.core.BlockStateOverrides.BlockStateOverride;
import org.dynmapblockscan.core.blockstate.BSBlockState;
import org.dynmapblockscan.core.blockstate.VariantList;
import org.dynmapblockscan.core.model.BlockModel;
import org.dynmapblockscan.core.statehandlers.StateContainer.StateRec;
import org.dynmapblockscan.fabric_1_20.statehandlers.FabricStateContainer;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class DynmapBlockScanPlugin extends AbstractBlockScanBase
{
    public static DynmapBlockScanPlugin plugin;
    public static FabricLoader flInst = FabricLoader.getInstance();

    public DynmapBlockScanPlugin(MinecraftServer srv)
    {
        plugin = this;
        logger = new OurLog();
    }

    public void buildAssetMap() {
    	assetmap = new HashMap<String, PathElement>();
        Iterator<ModContainer> mcl = flInst.getAllMods().stream().iterator();

        while (mcl.hasNext()) {
        	ModContainer mc = mcl.next();
        	String mid = mc.getMetadata().getId().toLowerCase();
            ModOrigin modOrigin = mc.getOrigin();
        	if (modOrigin == null) continue;

            if(modOrigin.getKind() == ModOrigin.Kind.NESTED) continue;;
            List<Path> modPaths = modOrigin.getPaths();
        	if (modPaths == null) continue;
        	File src = modPaths.get(0).toFile();
        	// Process mod file
        	processModFile(mid, src);
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
        for (ModContainer mod : flInst.getAllMods()) {

        	loadModuleOverrideResources(mod.getMetadata().getId());
        }
        Map<String, BlockRecord> blockRecords = new LinkedHashMap<String, BlockRecord>();

    	logger.info("Start processing states");

        // Now process models from block records
        Map<String, BlockModel> models = new LinkedHashMap<String, BlockModel>();

    	DefaultedRegistry<Block> bsids = Registries.BLOCK;
        Block baseb = null;

        Iterator<Block> iter = bsids.iterator();
        // Scan blocks and block states
        while (iter.hasNext()) {
            Block b = iter.next();
            if (b == baseb) { continue; }
            baseb = b;
            RegistryKey<Block> rl = Registries.BLOCK.getKey(b).orElse(null);
            StateManager<Block, BlockState> bsc = b.getStateManager();
            // See if any of the block states use MODEL
            boolean uses_model = false;
            boolean uses_nonmodel = false;

            for (BlockState bs : bsc.getStates()) {
            	switch (bs.getRenderType()) {
            		case MODEL:
            			uses_model = true;
            			break;
            		case INVISIBLE:
            			uses_nonmodel = true;
            			if (verboselogging)
            			    logger.info(String.format("%s: Invisible block - nothing to render", rl));
            			break;
            		case ENTITYBLOCK_ANIMATED:
            			uses_nonmodel = true;
                        if (verboselogging)
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
            Map<String, List<String>> propMap = buildPropoertyMap(bsc);
            // Try to find blockstate file
            //Material mat = b.getDefaultState().getBlock().getM
            MapColor matcol = b.getDefaultMapColor();
            BSBlockState blockstate = loadBlockState(rl.getValue().getNamespace(), rl.getValue().getPath(), overrides, propMap);
            // Build block record
            BlockRecord br = new BlockRecord();
            // Process blockstate
        	if (blockstate != null) {
                br.renderProps = blockstate.getRenderProps();
                br.materialColorID = MaterialColorID.byID(matcol.id);
                br.lightAttenuation = 15;
                try {	// Workaround for mods with broken block state logic...
                	br.lightAttenuation = b.getDefaultState().isSolidBlock(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) ? 15 : (b.getDefaultState().isTransparent(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) ? 0 : 1);
                } catch (Exception x) {
                	logger.warning(String.format("Exception while checking lighting data for block state: %s", b));
                	logger.verboseinfo("Exception: " + x.toString());
                }
        	}
        	// Build generic block state container for block
        	br.sc = new FabricStateContainer(b, br.renderProps, propMap);
        	if (blockstate != null) {
                BlockStateOverride ovr = overrides.getOverride(rl.getValue().getNamespace(), rl.getValue().getPath());
            	br.varList = new LinkedHashMap<StateRec, List<VariantList>>();
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
            // Check for matching handler
            blockRecords.put(rl.getValue().toString(), br);
        }

        logger.info("Loading models....");
        loadModels(blockRecords, models);
        logger.info("Variant models loaded");
        // Now, resolve all parent references - load additional models
        resolveParentReferences(models);
        logger.info("Parent models loaded and resolved");
        resolveAllElements(blockRecords, models);
        logger.info("Elements generated");

        publishDynmapModData();

        assetmap = null;
    }

    @Override
    public InputStream openResource(String modid, String rname) {
        if (modid.equals("minecraft")) modid = "dynmapblockscan";   // We supply resources (1.13.2 doesn't have assets in server jar)
        String rname_lc = rname.toLowerCase();
        Object mod =  flInst.getObjectShare().get(modid + ":" + rname_lc);

        ClassLoader cl = MinecraftServer.class.getClassLoader();
        if (mod != null) {
            cl = mod.getClass().getClassLoader();
        }
        if (cl != null) {
            InputStream is = cl.getResourceAsStream(rname_lc);
            if (is == null) {
                is = cl.getResourceAsStream(rname);
            }
            return is;
        }
        return null;
    }

    public Map<String, List<String>> buildPropoertyMap(StateManager<Block, BlockState> bsc) {
    	Map<String, List<String>> renderProperties = new LinkedHashMap<String, List<String>>();
		// Build table of render properties and valid values
		for (Property<?> p : bsc.getProperties()) {
			String pn = p.getName();
			ArrayList<String> pvals = new ArrayList<String>();
			for (Comparable<?> val : p.getValues()) {
				if (val instanceof StringIdentifiable) {
					pvals.add(((StringIdentifiable)val).asString());
				}
				else {
					pvals.add(val.toString());
				}
			}
			renderProperties.put(pn, pvals);
		}
		return renderProperties;
    }

    // Build Map<String, String> from properties in BlockState
    public Map<String, String> fromBlockState(BlockState bs) {
    	ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
    	for (Property<?> x : bs.getProperties()) {
    	    Object v = bs.get(x);
    		if (v instanceof StringIdentifiable) {
    			bld.put(x.getName(), ((StringIdentifiable)v).asString());
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

