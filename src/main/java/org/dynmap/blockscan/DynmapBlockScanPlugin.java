package org.dynmap.blockscan;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dynmap.blockscan.BlockStateOverrides.BlockStateOverride;
import org.dynmap.blockscan.blockstate.BlockState;
import org.dynmap.blockscan.blockstate.ModelRotation;
import org.dynmap.blockscan.blockstate.Variant;
import org.dynmap.blockscan.blockstate.VariantList;
import org.dynmap.blockscan.model.BlockElement;
import org.dynmap.blockscan.model.BlockFace;
import org.dynmap.blockscan.model.BlockModel;
import org.dynmap.blockscan.statehandlers.BedMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandler;
import org.dynmap.blockscan.statehandlers.IStateHandlerFactory;
import org.dynmap.blockscan.statehandlers.PistonMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SimpleMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.SnowyMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.StairMetadataStateHandler;
import org.dynmap.blockscan.statehandlers.StateContainer;
import org.dynmap.blockscan.statehandlers.StateContainer.StateRec;
import org.dynmap.modsupport.BlockSide;
import org.dynmap.modsupport.BlockTextureRecord;
import org.dynmap.modsupport.GridTextureFile;
import org.dynmap.modsupport.ModSupportAPI;
import org.dynmap.modsupport.ModTextureDefinition;
import org.dynmap.modsupport.TextureModifier;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class DynmapBlockScanPlugin
{
    public static OurLog logger = new OurLog();
    public static DynmapBlockScanPlugin plugin;
    
    private Map<EnumFacing, BlockSide> faceToSide = new HashMap<EnumFacing, BlockSide>();
    

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
        
        faceToSide.put(EnumFacing.DOWN, BlockSide.FACE_0);
        faceToSide.put(EnumFacing.UP, BlockSide.FACE_1);
        faceToSide.put(EnumFacing.NORTH, BlockSide.FACE_2);
        faceToSide.put(EnumFacing.SOUTH, BlockSide.FACE_3);
        faceToSide.put(EnumFacing.WEST, BlockSide.FACE_4);
        faceToSide.put(EnumFacing.EAST, BlockSide.FACE_5);
    }


    public void onEnable() {
        logger.info("onEnable()");
    }
    public void onDisable() {
        logger.info("onDisable()");
    }
    public void serverStarted() {
    	logger.info("serverStarted()");
    }
    public void serverStarting() {
        logger.info("serverStarting()");
        
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
            //logger.info(String.format("Block %s: %d", rl, Block.getIdFromBlock(b)));
            BlockStateContainer bsc = b.getBlockState();
            // See if any of the block states use MODEL
            boolean uses_model = false;
            boolean uses_nonmodel = false;
            for (IBlockState bs : bsc.getValidStates()) {
            	switch (bs.getRenderType()) {
            		case MODEL:
            			uses_model = true;
            			break;
            		case INVISIBLE:
            			uses_nonmodel = true;
            			logger.info(String.format("%s: Invisible block - nothing to render", rl));
            			break;
            		case ENTITYBLOCK_ANIMATED:
            			uses_nonmodel = true;
            			logger.info(String.format("%s: Animated block - needs to be handled specially", rl));
            			break;
            		case LIQUID:
            			uses_nonmodel = true;
            			logger.info(String.format("%s: Liquid block - special handling", rl));
            			break;
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
        	else {
        	    br.varList = Collections.emptyMap();
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
        }
        
        // Now process models from block records
        Map<String, BlockModel> models = new HashMap<String, BlockModel>();
        logger.info("Loading models....");
        for (String blkname : blockRecords.keySet()) {
        	BlockRecord br = blockRecords.get(blkname);
        	if (br.sc != null) {
        		for (Entry<StateRec, List<VariantList>> var : br.varList.entrySet()) {
        			for (VariantList vl : var.getValue()) {
        				for (Variant va : vl.variantList) {
        					if (va.model != null) {
        						String[] tok = va.model.split(":");
        						if (tok.length == 1) {
        							tok = new String[] { "minecraft", "block/" + tok[0] };
        						}
        						else {
        							tok[1] = "block/" + tok[1];
        						}
        						String modid = tok[0] + ":" + tok[1];
        						BlockModel mod = models.get(modid);	// See if we have it
        						if (mod == null) {
        							mod = loadBlockModelFile(tok[0], tok[1]);
        							if (mod != null) {
        								models.put(modid, mod);
        							}
        						}
        						va.modelID = modid;	// save normalized ID
        					}
        				}
        			}
        		}
        	}
        }
        logger.info("Variant models loaded");
        // Now, resolve all parent references - load additional models
        LinkedList<BlockModel> modelToResolve = new LinkedList<BlockModel>(models.values());
        while (modelToResolve.isEmpty() == false) {
        	BlockModel mod = modelToResolve.pop();
        	if (mod.parent != null) {	// If parent reference
        		String modid = mod.parent;
        		if (modid.indexOf(':') < 0) {
        			modid = "minecraft:" + modid;
        		}
        		mod.parentModel = models.get(modid);	// Look up: see if already loaded
        		if (mod.parentModel == null) {
					String[] tok = modid.split(":");
					mod.parentModel = loadBlockModelFile(tok[0], tok[1]);
					if (mod.parentModel != null) {
						models.put(modid, mod.parentModel);
						modelToResolve.push(mod.parentModel);
						//logger.info("Loaded parent " + modid);
					}
        		}
        	}
        }
        logger.info("Parent models loaded and resolved");
        // Now resolve the elements for all the variants
        for (String blkname : blockRecords.keySet()) {
        	BlockRecord br = blockRecords.get(blkname);
        	if (br.sc != null) {
        		for (Entry<StateRec, List<VariantList>> var : br.varList.entrySet()) {
        			for (VariantList vl : var.getValue()) {
        				for (Variant va : vl.variantList) {
        					if(va.generateElements(models) == false) {
        						logger.warning(va.toString() + ": failed to generate elements");
        					}
        					else {
        						if ((va.elements.size() == 1) && (va.elements.get(0).isSimpleBlock())) {
        							if (br.handler != null) {
        								//logger.info(String.format("%s: %s is simple block with %s map",  blkname, var.getKey(), br.handler.getName()));
        								registerSimpleDynmapCubes(blkname, var.getKey().metadata, va.elements.get(0), va.rotation, va.uvlock);
        							}
        						}
        					}
        				}
        			}
        		}
        	}
        }
        logger.info("Elements generated");
        
        publishDynmapModData();
    }
    
    private ModSupportAPI dynmap_api;
    
    private static class ModDynmapRec {
    	ModTextureDefinition txtDef;
    	Map<String, GridTextureFile> textureIDsByPath = new HashMap<String, GridTextureFile>();
    	int nextTxtID = 1;
    	
    	public GridTextureFile registerTexture(String txtpath) {
    		GridTextureFile txtf = textureIDsByPath.get(txtpath);
    		if (txtf == null) {
    			String txtid = String.format("txt%04d", nextTxtID);
    			nextTxtID++;	// Assign next ID
    			// Split path to build full path
    			String[] ptok = txtpath.split(":");
    			String fname = "assets/" + ptok[0] + "/textures/" + ptok[1] + ".png";
    			txtf = txtDef.registerTextureFile(txtid, fname);
    			if (txtf != null) {
    				textureIDsByPath.put(txtpath, txtf);
    			}
    		}
    		return txtf;
    	}
    }
    private Map<String, ModDynmapRec> modTextureDef = new HashMap<String, ModDynmapRec>();
    
        
    public void registerSimpleDynmapCubes(String blkname, int[] meta, BlockElement element, ModelRotation rot, boolean uvlock) {
    	String[] tok = blkname.split(":");
    	String modid = tok[0];
    	String blknm = tok[1];
    	if (tok[0].equals("minecraft")) {	// Skip vanilla
    		return;
    	}
    	if (dynmap_api == null) {
    		dynmap_api = ModSupportAPI.getAPI();
        	if (dynmap_api == null) {
        		return;
        	}
    	}
    	ModDynmapRec td = modTextureDef.get(modid);
    	if (td == null) {
    		td = new ModDynmapRec();
    		td.txtDef = dynmap_api.getModTextureDefinition(modid, null);
    		if (td.txtDef == null) {
    			return;
    		}
    		modTextureDef.put(modid, td);
    		logger.info("Create dynmap mod record for " + modid);
    	}
    	// Create block texture record
    	BlockTextureRecord btr = td.txtDef.addBlockTextureRecord(blknm);
    	if (btr == null) {
    		return;
    	}
    	logger.info("Created block record for " + blkname + Arrays.toString(meta));
    	// Set matching metadata
    	for (int metaval : meta) {
    		btr.setMetaValue(metaval);
    	}
    	// Loop over the images for the element
    	for (Entry<EnumFacing, BlockFace> face : element.faces.entrySet()) {
    		BlockFace f = face.getValue();
    		BlockSide bs = faceToSide.get(face.getKey());
    		if ((bs != null) && (f.texture != null)) {
    			GridTextureFile gtf = td.registerTexture(f.texture);
				TextureModifier tm = TextureModifier.NONE;
				if (!uvlock) {
					switch (rot.rotateVertex(face.getKey(), 0)) {
						case 1:
							tm = TextureModifier.ROT90;
							break;
						case 2:
							tm = TextureModifier.ROT180;
							break;
						case 3:
							tm = TextureModifier.ROT270;
							break;
					}
				}
				btr.setSideTexture(gtf, tm, bs);
    		}
    	}
    }
    
    public void publishDynmapModData() {
    	for (ModDynmapRec mod : modTextureDef.values()) {
    		if (mod.txtDef != null) {
    			mod.txtDef.publishDefinition();
    			logger.info("Published " + mod.txtDef.getModID() + " to Dynmap");
    		}
    	}
    
    }
    
    public static InputStream openResource(String modid, String rname) {
        String rname_lc = rname.toLowerCase();
        if (modid != null) {
            ModContainer mc = Loader.instance().getIndexedModList().get(modid);
            Object mod = (mc != null)?mc.getMod():null;
            if (mod != null) {
                InputStream is = mod.getClass().getClassLoader().getResourceAsStream(rname);
                if (is == null) {
                    is = mod.getClass().getClassLoader().getResourceAsStream(rname_lc);
                }
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
            if (is == null) {
                is = mod.getClass().getClassLoader().getResourceAsStream(rname_lc);
            }
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
        	try {
        	    bs = parse.fromJson(rdr, BlockState.class);
        	} catch (Exception x) {
        	    logger.severe("Error processing " + path, x);
        	}
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
    
    private static BlockModel loadBlockModelFile(String modid, String respath) {
        String path = "assets/" + modid + "/models/" + respath + ".json";
    	BlockModel bs = null;
        InputStream is = openResource(modid, path);
        if (is != null) {	// Found it?
        	Reader rdr = new InputStreamReader(is, Charsets.UTF_8);
        	Gson parse = BlockModel.buildParser();	// Get parser
        	bs = parse.fromJson(rdr, BlockModel.class);
        	try {
				is.close();
			} catch (IOException e) {
			}
        	if (bs == null) {
        		logger.info(String.format("%s:%s : Failed to load model!", modid, path));
        	}
        }
        else {
    		logger.info(String.format("%s:%s : Failed to open model", modid, path));
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

