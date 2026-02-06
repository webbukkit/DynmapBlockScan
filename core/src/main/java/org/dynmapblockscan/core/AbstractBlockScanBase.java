package org.dynmapblockscan.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dynmap.modsupport.BlockSide;
import org.dynmap.modsupport.BlockTextureRecord;
import org.dynmap.modsupport.CuboidBlockModel;
import org.dynmap.modsupport.ModModelDefinition;
import org.dynmap.modsupport.ModSupportAPI;
import org.dynmap.modsupport.ModTextureDefinition;
import org.dynmap.modsupport.ModelBlockModel;
import org.dynmap.modsupport.PatchBlockModel;
import org.dynmap.modsupport.TextureFile;
import org.dynmap.modsupport.TextureModifier;
import org.dynmap.modsupport.TransparencyMode;
import org.dynmap.modsupport.ModelBlockModel.ModelBlock;
import org.dynmap.modsupport.ModelBlockModel.SideRotation;
import org.dynmap.renderer.RenderPatchFactory.SideVisible;
import org.dynmapblockscan.core.BlockStateOverrides.BlockStateOverride;
import org.dynmapblockscan.core.BlockStateOverrides.BlockTintOverride;
import org.dynmapblockscan.core.blockstate.BSBlockState;
import org.dynmapblockscan.core.blockstate.ElementFace;
import org.dynmapblockscan.core.blockstate.Variant;
import org.dynmapblockscan.core.blockstate.VariantList;
import org.dynmapblockscan.core.model.BlockElement;
import org.dynmapblockscan.core.model.BlockFace;
import org.dynmapblockscan.core.model.BlockModel;
import org.dynmapblockscan.core.statehandlers.StateContainer;
import org.dynmapblockscan.core.statehandlers.StateContainer.StateRec;
import org.dynmapblockscan.core.statehandlers.StateContainer.WellKnownBlockClasses;
import org.dynmapblockscan.core.util.Matrix3D;
import org.dynmapblockscan.core.util.Vector3D;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.nio.charset.StandardCharsets;

public abstract class AbstractBlockScanBase {
	public static BlockScanLog logger;
    public static boolean verboselogging = false;
    protected BlockStateOverrides overrides;
    public Set<String> disabledModules = new HashSet<String>();
    public Set<String> disabledBlockNames = new HashSet<String>();

    public enum MaterialColorID {
    	NONE(0),
        GRASS(1),
        SAND(2),
        WOOL(3),
        FIRE(4),
        ICE(5),
        METAL(6),
        PLANT(7),
        SNOW(8),
        CLAY(9),
        DIRT(10),
        STONE(11),
        WATER(12),
        WOOD(13),
        QUARTZ(14),
        COLOR_ORANGE(15),
        COLOR_MAGENTA(16),
        COLOR_LIGHT_BLUE(17),
        COLOR_YELLOW(18),
        COLOR_LIGHT_GREEN(19),
        COLOR_PINK(20),
        COLOR_GRAY(21),
        COLOR_LIGHT_GRAY(22),
        COLOR_CYAN(23),
        COLOR_PURPLE(24),
        COLOR_BLUE(25),
        COLOR_BROWN(26),
        COLOR_GREEN(27),
        COLOR_RED(28),
        COLOR_BLACK(29),
        GOLD(30),
        DIAMOND(31),
        LAPIS(32),
        EMERALD(33),
        PODZOL(34),
        NETHER(35),
        TERRACOTTA_WHITE(36),
        TERRACOTTA_ORANGE(37),
        TERRACOTTA_MAGENTA(38),
        TERRACOTTA_LIGHT_BLUE(39),
        TERRACOTTA_YELLOW(40),
        TERRACOTTA_LIGHT_GREEN(41),
        TERRACOTTA_PINK(42),
        TERRACOTTA_GRAY(43),
        TERRACOTTA_LIGHT_GRAY(44),
        TERRACOTTA_CYAN(45),
        TERRACOTTA_PURPLE(46),
        TERRACOTTA_BLUE(47),
        TERRACOTTA_BROWN(48),
        TERRACOTTA_GREEN(49),
        TERRACOTTA_RED(50),
        TERRACOTTA_BLACK(51),
        CRIMSON_NYLIUM(52),
        CRIMSON_STEM(53),
        CRIMSON_HYPHAE(54),
        WARPED_NYLIUM(55),
        WARPED_STEM(56),
        WARPED_HYPHAE(57),
        WARPED_WART_BLOCK(58),
        DEEPSLATE(59),
        RAW_IRON(60),
        GLOW_LICHEN(61);
    	
    	public int colorID;
    	
    	MaterialColorID(int id) {
    		colorID = id;
    	}
    	public static MaterialColorID byID(int id) {
    		for (MaterialColorID v : values()) {
    			if (v.colorID == id) return v;
    		}
    		return null;
    	}
    }

    //private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static class BlockRecord {
    	public StateContainer sc;
    	public Map<StateRec, List<VariantList>> varList;	// Model references for block
    	public Set<String> renderProps;	// Set of render properties
    	public MaterialColorID materialColorID;
    	public int lightAttenuation;
    }

    protected static class PathElement {
    	String[] modids;
    	PathElement(String mid) {
    		modids = new String[] { mid };
    	}
    	void addModId(String mid) {
    		modids = Arrays.copyOf(modids, modids.length + 1);
    		modids[modids.length-1] = mid;
    	}
    }
    protected static class PathDirectory extends PathElement {
    	Map<String,PathElement> entries = new HashMap<String, PathElement>();
    	PathDirectory(String mid) {
    		super(mid);
    	}
    	
    }
    protected static Map<String, PathElement> assetmap;	// Map of asset paths and mods containing them

    protected void addElement(String modid, String n) {
    	String[] tok = n.split("/");
    	PathElement pe;
    	Map<String, PathElement> m = assetmap;
    	for (int i = 0; i < (tok.length - 1); i++) {	// Handle directory
    		if (tok[i].equals(".")) {	// Skip dot path elements
    		}
    		else {
    			pe = m.get(tok[i]);
    			// New - add directory record
    			if (pe == null) {
    				pe = new PathDirectory(modid);
    				m.put(tok[i], pe);	// Add to parent
    			}
    			// If existing is file record, promote
    			else if ((pe instanceof PathDirectory) == false) {
    				PathElement pe2 = new PathDirectory(modid);
    				for (String mm : pe.modids) {
    					pe2.addModId(mm);
    				}
    				m.put(tok[i], pe2);	// Add to parent
    				pe = pe2;
    			}
    			else {
    				pe.addModId(modid);
    			}
				m = ((PathDirectory)pe).entries;
    		}
    	}
    	// Look for file record
    	pe = m.get(tok[tok.length - 1]);
    	if (pe == null) {
    		pe = new PathElement(modid);
    		m.put(tok[tok.length - 1], pe);
    	}
    	else {
    		pe.addModId(modid);
    	}
    }
    private static PathElement findElement(Map<String, PathElement> m, String pth) {
    	String[] tok = pth.split("/");
    	for (int i = 0; i < (tok.length - 1); i++) {	// Handle directory
    		if (tok[i].equals(".")) {	// Skip dot path elements
    			continue;
    		}
    		PathElement pe = m.get(tok[i]);
    		if (pe instanceof PathDirectory) {
    			m = ((PathDirectory)pe).entries;
    		}
    		else {
    			return null;
    		}
    	}
    	return m.get(tok[tok.length - 1]);
    }
    
    public void setDisabledModules(List<String> modules) {
    	disabledModules.addAll(modules);
    }

    public void setDisabledBlockNames(List<String> blocknames) {
    	disabledBlockNames.addAll(blocknames);
    }

    public boolean isDisabledModule(String modid) {
    	return disabledModules.contains(modid);
    }

    public boolean isDisabledBlockName(String blockname) {
    	return disabledBlockNames.contains(blockname);
    }

    protected String scanForElement(Map<String, PathElement> m, String base, String fname) {
    	for (Entry<String, PathElement> me : m.entrySet()) {
    		PathElement p = me.getValue();
    		if (p instanceof PathDirectory) {
    			PathDirectory pd = (PathDirectory) p;
    			String rslt = scanForElement(pd.entries, base + "/" + me.getKey(), fname);
    			if (rslt != null) return rslt;
    		}
    		else if (me.getKey().equals(fname)) {
    			return base + "/" + me.getKey();
    		}
    	}
    	return null;
    }
    
    public abstract InputStream openResource(String modid, String rname);

    public InputStream openAssetResource(String modid, String subpath, String resourcepath, boolean scan) {
    	String pth = "assets/" + modid + "/" + subpath + "/" + resourcepath;
    	PathElement pe = findElement(assetmap, pth);
    	InputStream is = null;
    	// If found, scan mods matching path
    	if (pe != null) {
    	    is = openResource(modid, pth);
    		if (is == null) {
    			for (String mid : pe.modids) {
    				is = openResource(mid, pth);
    				if (is != null) return is;
    			}
    		}
    	}
    	// If not found, look for resource under subpath (some mods do this for blockstate...)
    	if ((is == null) && scan) {
    		pth = "assets/" + modid + "/" + subpath;
    		pe = findElement(assetmap, pth);	// Find subpath
    		if (pe instanceof PathDirectory) {
    			pth = scanForElement(((PathDirectory)pe).entries, pth, resourcepath);
    			if (pth != null) {
    				is = openResource(modid, pth);
    			}
    		}
    	}
    	return is;
    }
    protected BlockModel loadBlockModelFile(String modid, String respath) {
    	// Handle respath NOT having a path under models
    	if (respath.indexOf('/') < 0) {
    		respath = "block/" + respath;
    	}
    	BlockModel bs = null;
        InputStream is = openAssetResource(modid, "models", respath + ".json", true);
        if (is != null) {	// Found it?
        	Reader rdr = new InputStreamReader(is, StandardCharsets.UTF_8);
        	Gson parse = BlockModel.buildParser();	// Get parser
        	try {
        	    JsonReader jrdr = new JsonReader(rdr);
        	    jrdr.setLenient(true);
        	    bs = parse.fromJson(jrdr, BlockModel.class);
        	} catch (JsonSyntaxException jsx) {
                logger.warning(String.format("%s:%s : JSON syntax error in model file", modid, respath));
        	}
        	try {
				is.close();
			} catch (IOException e) {
			}
        	if (bs == null) {
        		logger.info(String.format("%s:%s : Failed to load model!", modid, respath));
                bs = new BlockModel();    // Return empty model
        	}
        }
        else {
    		logger.info(String.format("%s:%s : Failed to open model", modid, respath));
    		bs = new BlockModel();    // Return empty model
        }
        return bs;
    }

    protected ModSupportAPI dynmap_api;
    
    public static class ModDynmapRec {
    	public ModTextureDefinition txtDef;
    	public ModModelDefinition modDef;
    	Map<String, TextureFile> textureIDsByPath = new HashMap<String, TextureFile>();
    	Set<String> textureIDs = new HashSet<String>();
    	
    	private String getTextureID(String txtpath) {
    		String[] tok = txtpath.split("/");
    		String base = tok[tok.length-1];
    		int idx = 1;
    		String id = base;
    		while (textureIDs.contains(id)) {
    			id = base + idx;
    			idx++;
    		}
    		textureIDs.add(id);
    		return id;
    	}
    	
    	public TextureFile registerTexture(String txtpath) {
    	    txtpath = txtpath.toLowerCase();
    		TextureFile txtf = textureIDsByPath.get(txtpath);
    		if (txtf == null) {
    			String txtid = getTextureID(txtpath);
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
        public TextureFile registerBiomeTexture(String txtpath) {
            TextureFile txtf = textureIDsByPath.get(txtpath);
            if (txtf == null) {
    			String txtid = getTextureID(txtpath);
                // Split path to build full path
                String[] ptok = txtpath.split(":");
                String fname = "assets/" + ptok[0] + "/textures/" + ptok[1] + ".png";
                txtf = txtDef.registerBiomeTextureFile(txtid, fname);
                if (txtf != null) {
                    textureIDsByPath.put(txtpath, txtf);
                }
            }
            return txtf;
        }
        // Create block texture record
        public BlockTextureRecord getBlockTxtRec(String blknm, Map<String, String> statemap) {
            BlockTextureRecord btr = txtDef.addBlockTextureRecord(blknm);
            if (btr == null) {
                return null;
            }
            if (verboselogging)
                logger.debug("Created block record for " + blknm + statemap.toString());
            // Set matching statemap
            btr.setBlockStateMapping(statemap);
            return btr;
        }
        // Create cuboid model
        public CuboidBlockModel getCuboidModelRec(String blknm, Map<String,String> statemap) {
            if (this.modDef == null) {
                this.modDef = this.txtDef.getModelDefinition();
            }
            CuboidBlockModel mod = this.modDef.addCuboidModel(blknm);
            if (verboselogging)
                logger.debug("Created cuboid model for " + blknm + statemap);
            // Set matching metadata
            mod.setBlockStateMapping(statemap);
            return mod;
        }
        // Create patch model
        public PatchBlockModel getPatchModelRec(String blknm, Map<String, String> statemap) {
            if (this.modDef == null) {
                this.modDef = this.txtDef.getModelDefinition();
            }
            PatchBlockModel mod = this.modDef.addPatchModel(blknm);
            if (verboselogging)
                logger.debug("Created patch model for " + blknm + statemap);
            // Set matching metadata
            mod.setBlockStateMapping(statemap);
            
            return mod;
        }
        // Create model block model
        public ModelBlockModel getModelBlockModelRec(String blknm, Map<String, String> statemap) {
            if (this.modDef == null) {
                this.modDef = this.txtDef.getModelDefinition();
            }
            ModelBlockModel mod = this.modDef.addModelBlockModel(blknm);
            if (verboselogging)
                logger.debug("Created model block model for " + blknm + statemap);
            
            mod.setBlockStateMapping(statemap);            
            return mod;
        }
        
    }
    protected Map<String, ModDynmapRec> modTextureDef = new LinkedHashMap<String, ModDynmapRec>();
    
    protected ModDynmapRec getModRec(String modid) {
        if (dynmap_api == null) {
            dynmap_api = ModSupportAPI.getAPI();
            if (dynmap_api == null) {
                return null;
            }
        }
        ModDynmapRec td = modTextureDef.get(modid);
        if (td == null) {
            td = new ModDynmapRec();
            td.txtDef = dynmap_api.getModTextureDefinition(modid, null);
            if (td.txtDef == null) {
                return null;
            }
            modTextureDef.put(modid, td);
            if (verboselogging)
                logger.debug("Create dynmap mod record for " + modid);
        }
        return td;
    }

    public void publishDynmapModData() {
    	for (ModDynmapRec mod : modTextureDef.values()) {
    		if (mod.txtDef != null) {
    			mod.txtDef.publishDefinition();
                logger.info("Published " + mod.txtDef.getModID() + " textures to Dynmap");
    		}
    		if (mod.modDef != null) {
                mod.modDef.publishDefinition();
                logger.info("Published " + mod.modDef.getModID() + " models to Dynmap");
            }
    	}
    
    }

    public void registerSimpleDynmapCubes(String blkname, StateRec state, BlockElement element, WellKnownBlockClasses type,
		MaterialColorID materialColorID, int lightAtten) {
    	String[] tok = blkname.split(":");
    	String modid = tok[0];
    	String blknm = tok[1];

    	if (isDisabledModule(modid) || isDisabledBlockName(blkname)) {
    		return;
    	}
    	// Get record for mod
    	ModDynmapRec td = getModRec(modid);
    	// Create block texture record
    	BlockTextureRecord btr = td.getBlockTxtRec(blknm, state.keyValuePairs);
    	if (btr == null) {
    		return;
    	}
    	// If not light blocking
    	if (lightAtten == 0) {
    		btr.setTransparencyMode(TransparencyMode.TRANSPARENT);
    	}
    	else if (lightAtten == 15) {
    		btr.setTransparencyMode(TransparencyMode.OPAQUE);
    	}
    	else {
    		btr.setTransparencyMode(TransparencyMode.SEMITRANSPARENT);    		
    	}
    	boolean tinting = false;   // Watch out for tinting
        for (BlockFace f : element.faces.values()) {
            if (f.tintindex >= 0) {
                tinting = true;
                break;
            }
        }
        // If block has tinting, try to figure out what to use
        if (tinting) {
            String txtfile = null;
            BlockTintOverride ovr = overrides.getTinting(modid, blknm, state.getProperties());
            if (ovr == null) { // No match, need to guess
                switch (materialColorID) {
                    case PLANT:
                        txtfile = "minecraft:colormap/foliage";
                        break;
                    case GRASS:
                    default:
                        txtfile = "minecraft:colormap/grass";
                        break;
                }
            }
            else {
                txtfile = ovr.colormap[0];
            }
            if (txtfile != null) {
                TextureFile gtf = td.registerBiomeTexture(txtfile);
                btr.setBlockColorMapTexture(gtf);
            }
        }
       
    	// Loop over the images for the element
    	for (Entry<ElementFace, BlockFace> face : element.faces.entrySet()) {
    	    ElementFace facing = face.getKey();
    		BlockFace f = face.getValue();
    		BlockSide bs = facing.side;
    		if ((bs != null) && (f.texture != null)) {
    			TextureFile gtf = td.registerTexture(f.texture);
				int faceidx = (360-f.rotation);
				if (!element.uvlock) {
				    faceidx = faceidx + f.facerotation;
				}
                TextureModifier tm = TextureModifier.NONE;
			    switch (faceidx % 360) {
					case 90:
						tm = TextureModifier.ROT90;
						break;
					case 180:
						tm = TextureModifier.ROT180;
						break;
					case 270:
						tm = TextureModifier.ROT270;
						break;
				}
				btr.setSideTexture(gtf, tm, bs);
    		}
    	}
    }
    
    public void registerModelListModel(String blkname, StateRec state, List<BlockElement> elems, WellKnownBlockClasses type, 
		MaterialColorID materialColorID, int lightAtten) {
        String[] tok = blkname.split(":");
        String modid = tok[0];
        String blknm = tok[1];
        if (isDisabledModule(modid) || isDisabledBlockName(blkname)) {   // Skip vanilla
            return;
        }
        // Get record for mod
        ModDynmapRec td = getModRec(modid);
        // Create block texture record
        BlockTextureRecord btr = td.getBlockTxtRec(blknm, state.keyValuePairs);
        if (btr == null) {
            return;
        }
        // Check for tinting and/or culling
        boolean tinting = false;   // Watch out for tinting
        for (BlockElement be : elems) {
            for (BlockFace f : be.faces.values()) {
                if (f.tintindex >= 0) {
                    tinting = true;
                    break;
                }
            }
        }
    	// If not light blocking
    	if (lightAtten == 0) {
    		btr.setTransparencyMode(TransparencyMode.TRANSPARENT);
    	}
    	else if (lightAtten == 15) {
    		btr.setTransparencyMode(TransparencyMode.OPAQUE);
    	}
    	else {
    		btr.setTransparencyMode(TransparencyMode.SEMITRANSPARENT);    		
    	}
        // If block has tinting, try to figure out what to use
        TextureModifier tintmodifier = TextureModifier.NONE;
        if (tinting) {
            String txtfile = null;
            BlockTintOverride ovr = overrides.getTinting(modid, blknm, state.getProperties());
            if (ovr == null) { // No match, need to guess
                switch (materialColorID) {
	                case PLANT:
	                	tintmodifier = TextureModifier.FOLIAGETONED;
	                    break;
	                case WATER:
	                	tintmodifier = TextureModifier.WATERTONED;
	                    break;
	                case GRASS:
	                	tintmodifier = TextureModifier.GRASSTONED;
	                	break;
	                default:
	                	tintmodifier = TextureModifier.GRASSTONED;
	                    break;
                }
            }
            else {
                txtfile = ovr.colormap[0];
            }
            if (txtfile != null) {
                TextureFile gtf = td.registerBiomeTexture(txtfile);
                btr.setBlockColorMapTexture(gtf);
            }
        }
        // Get model list model
        ModelBlockModel mod = td.getModelBlockModelRec(blknm, state.keyValuePairs);
        // Start patch list
        ArrayList<String> textures = new ArrayList<String>();
        // Loop over elements        
        for (BlockElement be : elems) {
        	double xrot = 0, yrot = 0, zrot = 0;
        	double[] origin = null;
        	if (be.rotation != null) {
        		if (be.rotation.axis != null) {
        			if (be.rotation.axis.equals("x")) {
        				xrot = be.rotation.angle;
        			}
        			else if (be.rotation.axis.equals("y")) {
        				yrot = be.rotation.angle;
        			}
        			else if (be.rotation.axis.equals("z")) {
        				zrot = be.rotation.angle;
        			}
        			if (be.rotation.origin != null) {
        				origin = be.rotation.origin;
        			}
        		}
        	}
            ModelBlock modelem = mod.addModelBlock(be.from, be.to, xrot, yrot, zrot, be.shade, origin,
            		be.modelrot.rotX, be.modelrot.rotY, be.modelrot.rotZ);
            // Loop over the images for the element
            for (Entry<ElementFace, BlockFace> face : be.faces.entrySet()) {
                ElementFace facing = face.getKey();
                BlockFace f = face.getValue();
                SideRotation rot = SideRotation.DEG0;
                switch (f.rotation) {
                	case 90:
                		rot = SideRotation.DEG90;
                		break;
                	case 180:
                		rot = SideRotation.DEG180;
                		break;
                	case 270:
                		rot = SideRotation.DEG270;
                		break;
                }
                int txtidx = -1;
                if (f.texture != null) {
                	txtidx = textures.indexOf(f.texture);	// See if already registered
                	if (txtidx < 0) {
                		txtidx = textures.size();
                        textures.add(f.texture);	// Add to list
                		TextureFile gtf = td.registerTexture(f.texture);	// Register if needed
                        btr.setPatchTexture(gtf, (f.tintindex >= 0) ? tintmodifier : TextureModifier.NONE, txtidx);	// And set texture to assigned index
                	}
                }
                modelem.addBlockSide(facing.side, f.uv, rot, txtidx, f.tintindex);
            }
        }
    }
    
    public void registerDynmapPatches(String blkname, StateRec state, List<BlockElement> elems, WellKnownBlockClasses type) {
        String[] tok = blkname.split(":");
        String modid = tok[0];
        String blknm = tok[1];
        if (isDisabledModule(modid) || isDisabledBlockName(blkname)) {   // Skip vanilla
            return;
        }
        // Get record for mod
        ModDynmapRec td = getModRec(modid);
        // Create block texture record
        BlockTextureRecord btr = td.getBlockTxtRec(blknm, state.keyValuePairs);
        if (btr == null) {
            return;
        }
        // Check for tinting and/or culling
        boolean tinting = false;   // Watch out for tinting
        boolean culling = false;
        for (BlockElement be : elems) {
            for (BlockFace f : be.faces.values()) {
                if (f.tintindex >= 0) {
                    tinting = true;
                    break;
                }
                if (f.cullface != null) {
                    culling = true;
                }
            }
        }
        // Set to transparent (or semitransparent if culling)
        if (culling) {
            btr.setTransparencyMode(TransparencyMode.SEMITRANSPARENT);
        }
        else {
            btr.setTransparencyMode(TransparencyMode.TRANSPARENT);
        }
        // If block has tinting, try to figure out what to use
        if (tinting) {
            String txtfile = null;
            BlockTintOverride ovr = overrides.getTinting(modid, blknm, state.getProperties());
            if (ovr == null) { // No match, need to guess
                switch (type) {
                case LEAVES:
                case VINES:
                    txtfile = "minecraft:colormap/foliage";
                    break;
                default:
                    txtfile = "minecraft:colormap/grass";
                    break;
                }
            }
            else {
                txtfile = ovr.colormap[0];
            }
            if (txtfile != null) {
                TextureFile gtf = td.registerBiomeTexture(txtfile);
                btr.setBlockColorMapTexture(gtf);
            }
        }
        // Get patch model
        PatchBlockModel mod = td.getPatchModelRec(blknm, state.keyValuePairs);
        // Loop over elements
        int patchidx = 0;
        for (BlockElement be : elems) {
            // Loop over the images for the element
            for (Entry<ElementFace, BlockFace> face : be.faces.entrySet()) {
                ElementFace facing = face.getKey();
                BlockFace f = face.getValue();
                BlockSide bs = facing.side;
                if ((bs != null) && (f.texture != null)) {
                    TextureFile gtf = td.registerTexture(f.texture);
                    int faceidx = (360-f.rotation);
                    if (!be.uvlock) {
                        faceidx = faceidx + f.facerotation;
                    }
                    TextureModifier tm = TextureModifier.NONE;
                    switch (faceidx % 360) {
                    case 90:
                        tm = TextureModifier.ROT90;
                        break;
                    case 180:
                        tm = TextureModifier.ROT180;
                        break;
                    case 270:
                        tm = TextureModifier.ROT270;
                        break;
                    }
                    // And add patch to to model
                    if (addPatch(mod, facing, be) != null) {
                        btr.setPatchTexture(gtf, tm, patchidx);
                        // Increment patch count
                        patchidx++;
                    }
                    else {
                        logger.info("Failed to add patch for " + blkname);
                    }
                }
            }
        }
    }
    protected String addPatch(PatchBlockModel mod, ElementFace facing, BlockElement be) {
        // First, do the rotation on the from/to
        Vector3D fromvec = new Vector3D(be.from[0], be.from[1], be.from[2]);
        Vector3D tovec = new Vector3D(be.to[0], be.to[1], be.to[2]);
        Vector3D originvec = new Vector3D();
        Vector3D uvec = new Vector3D();
        Vector3D vvec = new Vector3D();
        // Now, compute corner vectors, based on which side
        switch (facing) {
            case DOWN:
                originvec.x = fromvec.x; originvec.y = fromvec.y; originvec.z = fromvec.z;
                uvec.x = tovec.x; uvec.y = fromvec.y; uvec.z = fromvec.z;
                vvec.x = fromvec.x; vvec.y = fromvec.y; vvec.z = tovec.z;
                break;
            case UP:
                originvec.x = fromvec.x; originvec.y = tovec.y; originvec.z = tovec.z;
                uvec.x = tovec.x; uvec.y = tovec.y; uvec.z = tovec.z;
                vvec.x = fromvec.x; vvec.y = tovec.y; vvec.z = fromvec.z;
                break;
            case WEST:
                originvec.x = fromvec.x; originvec.y = fromvec.y; originvec.z = fromvec.z;
                uvec.x = fromvec.x; uvec.y = fromvec.y; uvec.z = tovec.z;
                vvec.x = fromvec.x; vvec.y = tovec.y; vvec.z = fromvec.z;
                break;
            case EAST:
                originvec.x = tovec.x; originvec.y = fromvec.y; originvec.z = tovec.z;
                uvec.x = tovec.x; uvec.y = fromvec.y; uvec.z = fromvec.z;
                vvec.x = tovec.x; vvec.y = tovec.y; vvec.z = tovec.z;
                break;
            case NORTH:
                originvec.x = tovec.x; originvec.y = fromvec.y; originvec.z = fromvec.z;
                uvec.x = fromvec.x; uvec.y = fromvec.y; uvec.z = fromvec.z;
                vvec.x = tovec.x; vvec.y = tovec.y; vvec.z = fromvec.z;
                break;
            case SOUTH:
                originvec.x = fromvec.x; originvec.y = fromvec.y; originvec.z = tovec.z;
                uvec.x = tovec.x; uvec.y = fromvec.y; uvec.z = tovec.z;
                vvec.x = fromvec.x; vvec.y = tovec.y; vvec.z = tovec.z;
                break;
        }
        if ((be.rotation != null) && (be.rotation.angle != 0)) {
            Matrix3D rot = new Matrix3D();
            Vector3D scale = new Vector3D(1, 1, 1);
            double rescale = (1.0 / Math.cos(Math.toRadians(be.rotation.angle))) - 1.0;
            if ("z".equals(be.rotation.axis)) {
                rot.rotateXY(be.rotation.angle);
                scale.x += rescale;
                scale.y += rescale;
            }
            else if ("x".equals(be.rotation.axis)) {
                rot.rotateYZ(be.rotation.angle);
                scale.y += rescale;
                scale.z += rescale;
            }
            else {
                rot.rotateXZ(be.rotation.angle);
                scale.x += rescale;
                scale.z += rescale;
            }
            Vector3D axis;
            if (be.rotation.origin != null) {
                axis = new Vector3D(be.rotation.origin[0], be.rotation.origin[1], be.rotation.origin[2]);
            }
            else {
                axis = new Vector3D(8, 8, 8);
            }
            // Now do rotation
            originvec.subtract(axis);
            uvec.subtract(axis);
            vvec.subtract(axis);
            rot.transform(originvec);
            rot.transform(uvec);
            rot.transform(vvec);
            if (be.rotation.rescale) {
                originvec.scale(scale);
                uvec.scale(scale);
                vvec.scale(scale);
            }
            originvec.add(axis);
            uvec.add(axis);
            vvec.add(axis);
        }
        // Now unit scale
        originvec.scale(1.0/16.0);
        uvec.scale(1.0/16.0);
        vvec.scale(1.0/16.0);

        // Now, add patch, based on facing
        return mod.addPatch(originvec.x, originvec.y, originvec.z, uvec.x, uvec.y, uvec.z, vvec.x, vvec.y, vvec.z, SideVisible.TOP);
    }
    
    protected BSBlockState loadBlockState(String modid, String respath, BlockStateOverrides override, Map<String, List<String>> propMap) {
    	BlockStateOverride ovr = override.getOverride(modid, respath);
    	BSBlockState bs = null;
    	if (ovr == null) {	// No override
    		bs = loadBlockStateFile(modid, respath);
    	}
    	else if (ovr.blockStateName != null) {	// Simple override
    		bs = loadBlockStateFile(modid, ovr.blockStateName);
    	}
    	else if (ovr.baseNameProperty != null) {	// MUltiple files based on base property
    		List<String> vals = propMap.get(ovr.baseNameProperty);	// Look up defned values
    		if (vals == null) {
    			logger.warning(String.format("%s:%s : bad baseNameProperty=%s",  modid, respath, ovr.baseNameProperty));;
    			return null;
    		}
    		bs = new BSBlockState();
    		bs.nestedProp = ovr.baseNameProperty;
    		bs.nestedValueMap = new HashMap<String, BSBlockState>();
    		for (String v : vals) {
    			BSBlockState bs2 = loadBlockStateFile(modid, v + ovr.nameSuffix);
    			if (bs2 != null) {
    				bs.nestedValueMap.put(v,  bs2);
    			}
    		}
    	}
		return bs;
    }
	protected BSBlockState loadBlockStateFile(String modid, String respath) {
    	// Default path
        String path = "assets/" + modid + "/blockstates/" + respath + ".json";
    	BSBlockState bs = null;
        InputStream is = openAssetResource(modid, "blockstates", respath + ".json", true);
        if (is == null) {	// Not found? scan for name under blockstates directory (some mods do this...)
        	
        }
        if (is != null) {	// Found it?
        	Reader rdr = new InputStreamReader(is, StandardCharsets.UTF_8);
        	Gson parse = BSBlockState.buildParser();	// Get parser
        	try {
                JsonReader jrdr = new JsonReader(rdr);
                jrdr.setLenient(true);
        	    bs = parse.fromJson(jrdr, BSBlockState.class);
        	} catch (JsonSyntaxException jsx) {
                logger.warning(String.format("%s:%s : JSON syntax error in block state file", modid, path));
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
	
    protected void resolveAllElements(Map<String, BlockRecord> blockRecords, Map<String, BlockModel> models) {
        // Now resolve the elements for all the variants
        for (String blkname : blockRecords.keySet()) {
        	BlockRecord br = blockRecords.get(blkname);
        	if (br.sc != null) {
        		for (Entry<StateRec, List<VariantList>> var : br.varList.entrySet()) {
        		    // Produce merged element lists : for now, ignore random weights and just use first element of each section
        		    List<BlockElement> elems = new ArrayList<BlockElement>();
                    for (VariantList vl : var.getValue()) {
                        if (vl.variantList.size() > 0) {
                            Variant va = vl.variantList.get(0);
                            
                            if (va.generateElements(models) == false) {
                                logger.debug(va.toString() + ": failed to generate elements for " + blkname + "[" + var.getKey() + "]");
                            }
                            else {
                                elems.addAll(va.elements);
                            }
                        }
                    }
                    // If single simple full cube
                    if ((elems.size() == 1) && (elems.get(0).isSimpleBlock())) {
                        //logger.info(String.format("%s: %s is simple block with %s map",  blkname, var.getKey(), br.handler.getName()));
                        registerSimpleDynmapCubes(blkname, var.getKey(), elems.get(0), br.sc.getBlockType(), br.materialColorID, br.lightAttenuation);
                    }
                    else {
                    	registerModelListModel(blkname, var.getKey(), elems, br.sc.getBlockType(), br.materialColorID, br.lightAttenuation);
                    }
        		}
        	}
        }    	
    }
    
    protected void processModFile(String mid, File src) {
    	if (src.isFile() && src.canRead()) {	// Is in Jar?
    		ZipFile zf = null;
    		int cnt = 0;
            try {
                zf = new ZipFile(src);
                if (zf != null) {
                	Enumeration<? extends ZipEntry> zenum = zf.entries();
					while(zenum.hasMoreElements()) {
						ZipEntry ze = zenum.nextElement();
                		String n = ze.getName().replace('\\', '/');
                		if (n.startsWith("assets/")) {	// Asset path?
                			addElement(mid, n);
                			cnt++;
                		}
                	}
                }
            } catch (IOException e) {
                logger.severe("Error opening mod - " + src.getPath());
            } finally {
            	if (zf != null) {
					try {
						zf.close();
					} catch (IOException e) {
					}
            	}
            }
        	logger.info("modid: " + mid + ", src=" + src.getAbsolutePath() + ", cnt=" + cnt);
    	}    	
    }
    
    protected void loadOverrideResources() {
        InputStream override_str = openResource("dynmapblockscan", "blockstateoverrides.json");
        if (override_str != null) {
        	Reader rdr = new InputStreamReader(override_str, StandardCharsets.UTF_8);
            GsonBuilder gb = new GsonBuilder(); // Start with builder
            gb.registerTypeAdapter(BlockTintOverride.class, new BlockTintOverride.Deserializer()); // Add Condition handler1
            Gson parse = gb.create();
            JsonReader jrdr = new JsonReader(rdr);
            jrdr.setLenient(true);
        	overrides = parse.fromJson(jrdr, BlockStateOverrides.class);
        	try {
				override_str.close();
			} catch (IOException e) {
			}
        }
        else {
        	logger.info("Failed to load block overrides");
        	overrides = new BlockStateOverrides();
        }
    }
    protected void loadModuleOverrideResources(String modid) {
        InputStream str = openAssetResource(modid, "dynmap", "blockstateoverrides.json", true);
        if (str != null) {
            Reader rdr = new InputStreamReader(str, StandardCharsets.UTF_8);
            GsonBuilder gb = new GsonBuilder(); // Start with builder
            gb.registerTypeAdapter(BlockTintOverride.class, new BlockTintOverride.Deserializer()); // Add Condition handler1
            Gson parse = gb.create();
            try {
                JsonReader jrdr = new JsonReader(rdr);
                jrdr.setLenient(true);
                BlockStateOverrides modoverrides = parse.fromJson(jrdr, BlockStateOverrides.class);
                if (modoverrides != null) {
                    overrides.merge(modoverrides);
                    logger.info("Loaded dynmap overrides from " + modid);
                }
            } catch (JsonIOException iox) {
                logger.severe("Error reading dynmap overrides from " + modid);
            } catch (JsonSyntaxException sx) {
                logger.severe("Error parsing dynmap overrides from " + modid);
            } finally {
                if (str != null) { try { str.close(); } catch (IOException iox) {} }
            }
        }
    }
    
	protected void loadModels(Map<String, BlockRecord> blockRecords, Map<String, BlockModel> models) {
	    for (String blkname : blockRecords.keySet()) {
	    	BlockRecord br = blockRecords.get(blkname);
	    	if (br.sc != null) {
	    		for (Entry<StateRec, List<VariantList>> var : br.varList.entrySet()) {
	    			for (VariantList vl : var.getValue()) {
	    				for (Variant va : vl.variantList) {
	    					if (va.model != null) {
	    						String[] tok = va.model.split(":");
	    						if (tok.length == 1) {
	    							tok = new String[] { "minecraft", tok[0] };
	    						}
	    						String modid = tok[0] + ":" + tok[1];
	    						BlockModel mod = models.get(modid);	// See if we have it
	    						if (mod == null) {
	    							mod = loadBlockModelFile(tok[0], tok[1]);
									models.put(modid, mod);
	    						}
	    						va.modelID = modid;	// save normalized ID
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
	}
	
    protected void resolveParentReferences(Map<String, BlockModel> models) {
        LinkedList<String> modelToResolve = new LinkedList<String>(models.keySet());
        while (modelToResolve.isEmpty() == false) {
        	String modelID = modelToResolve.pop();
        	BlockModel mod = models.get(modelID);

        	//logger.info("mod: " + modelID + "=" + gson.toJson(mod));

        	if (mod.parent != null) {	// If parent reference
        		String modid = mod.parent;
        		if (modid.indexOf(':') < 0) {
        			modid = "minecraft:" + modid;
        		}
//        		logger.info("resolveParentReference: " + modid + " for " + modelID);
//                if (modid.equals("minecraft:block/button")) {
//                	logger.info("mod: " + modelID + "=" + gson.toJson(mod));
//                }

        		mod.parentModel = models.get(modid);	// Look up: see if already loaded
        		
        		if (mod.parentModel == null) {
					String[] tok = modid.split(":");
                    if (tok.length < 2) break; // index out of bounds is possible here, if so, we should skip
					mod.parentModel = loadBlockModelFile(tok[0], tok[1]);
					models.put(modid, mod.parentModel);
					modelToResolve.push(modid);
        		}
        		
            	//logger.info("mod (end): " + gson.toJson(mod));
            	
        	}
        }
    }

}
