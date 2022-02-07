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
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dynmap.modsupport.BlockSide;
import org.dynmap.modsupport.BlockTextureRecord;
import org.dynmap.modsupport.CuboidBlockModel;
import org.dynmap.modsupport.ModModelDefinition;
import org.dynmap.modsupport.ModSupportAPI;
import org.dynmap.modsupport.ModTextureDefinition;
import org.dynmap.modsupport.PatchBlockModel;
import org.dynmap.modsupport.TextureFile;
import org.dynmap.modsupport.TextureModifier;
import org.dynmap.modsupport.TransparencyMode;
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
import org.dynmapblockscan.core.statehandlers.BedMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.DoorStateHandler;
import org.dynmapblockscan.core.statehandlers.GateMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.IStateHandler;
import org.dynmapblockscan.core.statehandlers.IStateHandlerFactory;
import org.dynmapblockscan.core.statehandlers.NSEWConnectedMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.NSEWUConnectedMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.PistonMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.RedstoneWireStateHandler;
import org.dynmapblockscan.core.statehandlers.SimpleMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.SnowyMetadataStateHandler;
import org.dynmapblockscan.core.statehandlers.StairMetadataStateHandler;
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
	public static Logger logger = Logger.getLogger("DynmapBlockScan");
    public static boolean verboselogging = false;
    protected BlockStateOverrides overrides;
    	
    protected IStateHandlerFactory[] state_handler = {
        new NSEWConnectedMetadataStateHandler(),
        new NSEWUConnectedMetadataStateHandler(),
        new RedstoneWireStateHandler(),
        new GateMetadataStateHandler(),
        new StairMetadataStateHandler(),
        new DoorStateHandler(),
        new PistonMetadataStateHandler(),
        new SnowyMetadataStateHandler(),
        new BedMetadataStateHandler(),
        new SimpleMetadataStateHandler(true),
        new SimpleMetadataStateHandler(false)
    };

    public static class BlockRecord {
    	public StateContainer sc;
    	public Map<StateRec, List<VariantList>> varList;	// Model references for block
    	public Set<String> renderProps;	// Set of render properties
    	public IStateHandler handler;		// Best handler
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
        String path = "assets/" + modid + "/models/" + respath + ".json";
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
                logger.warning(String.format("%s:%s : JSON syntax error in model file", modid, path));
        	}
        	try {
				is.close();
			} catch (IOException e) {
			}
        	if (bs == null) {
        		logger.info(String.format("%s:%s : Failed to load model!", modid, path));
                bs = new BlockModel();    // Return empty model
        	}
        }
        else {
    		logger.info(String.format("%s:%s : Failed to open model", modid, path));
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
        public BlockTextureRecord getBlockTxtRec(String blknm, int[] meta) {
            BlockTextureRecord btr = txtDef.addBlockTextureRecord(blknm);
            if (btr == null) {
                return null;
            }
            if (verboselogging)
                logger.fine("Created block record for " + blknm + Arrays.toString(meta));
            // Set matching metadata
            for (int metaval : meta) {
                btr.setMetaValue(metaval);
            }
            return btr;
        }
        // Create cuboid model
        public CuboidBlockModel getCuboidModelRec(String blknm, int[] meta) {
            if (this.modDef == null) {
                this.modDef = this.txtDef.getModelDefinition();
            }
            CuboidBlockModel mod = this.modDef.addCuboidModel(blknm);
            if (verboselogging)
                logger.fine("Created cuboid model for " + blknm + Arrays.toString(meta));
            // Set matching metadata
            for (int metaval : meta) {
                mod.setMetaValue(metaval);
            }
            return mod;
        }
        // Create patch model
        public PatchBlockModel getPatchModelRec(String blknm, int[] meta) {
            if (this.modDef == null) {
                this.modDef = this.txtDef.getModelDefinition();
            }
            PatchBlockModel mod = this.modDef.addPatchModel(blknm);
            if (verboselogging)
                logger.fine("Created patch model for " + blknm + Arrays.toString(meta));
            // Set matching metadata
            for (int metaval : meta) {
                mod.setMetaValue(metaval);
            }
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
                logger.fine("Create dynmap mod record for " + modid);
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

    public void registerSimpleDynmapCubes(String blkname, StateRec state, BlockElement element, WellKnownBlockClasses type) {
    	String[] tok = blkname.split(":");
    	String modid = tok[0];
    	String blknm = tok[1];
    	int[] meta = state.metadata;
    	if (tok[0].equals("minecraft")) {	// Skip vanilla
    		return;
    	}
        // Temporary hack to avoid registering metadata duplicates
    	meta = pruneDuplicateMeta(blkname, meta); 
    	if (meta.length == 0) {
            return;
        }

    	// Get record for mod
    	ModDynmapRec td = getModRec(modid);
    	// Create block texture record
    	BlockTextureRecord btr = td.getBlockTxtRec(blknm, meta);
    	if (btr == null) {
    		return;
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
    // Temporary fix to avoid registering duplicate metadata values (until we get the block state mapping working right...)
    private Set<String> registeredBlockMeta = new HashSet<String>();

    protected int[] pruneDuplicateMeta(String blkname, int[] meta) {
        List<Integer> goodmeta = new ArrayList<Integer>();
        for (int mv : meta) {
            String blkid = blkname + ":" + mv;
            if (registeredBlockMeta.contains(blkid) == false) {
                registeredBlockMeta.add(blkid);
                goodmeta.add(mv);
            }
        }
        if (meta.length != goodmeta.size()) {
            int[] newmeta = new int[goodmeta.size()];
            for (int i = 0; i < newmeta.length; i++) {
                newmeta[i] = goodmeta.get(i);
            }
            meta = newmeta;
        }
        return meta;
    }
    
    public boolean isSimpleCuboid(List<BlockElement> elements) {
        for (BlockElement be : elements) {
            if (be.isSimpleCuboid() == false) {
                return false;
            }
        }
        return true;
    }
    
    public void registerSimpleDynmapCuboids(String blkname, StateRec state, List<BlockElement> elems, WellKnownBlockClasses type) {
        String[] tok = blkname.split(":");
        String modid = tok[0];
        String blknm = tok[1];
        int[] meta = state.metadata;
        if (tok[0].equals("minecraft")) {   // Skip vanilla
            return;
        }
        // Temporary hack to avoid registering metadata duplicates
        meta = pruneDuplicateMeta(blkname, meta); 
        if (meta.length == 0) {
            return;
        }
        // Get record for mod
        ModDynmapRec td = getModRec(modid);
        // Create block texture record
        BlockTextureRecord btr = td.getBlockTxtRec(blknm, meta);
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
        // Get cuboid model
        CuboidBlockModel mod = td.getCuboidModelRec(blknm, meta);
        // Loop over elements
        int imgidx = 0;
        int[] cuboididx = new int[6];
        for (BlockElement be : elems) {
            // Initialize to no texture for each side
            for (int v = 0; v < cuboididx.length; v++) cuboididx[v] = -1;
            // Loop over the images for the element
            for (Entry<ElementFace, BlockFace> face : be.faces.entrySet()) {
                ElementFace facing = face.getKey();
                BlockFace f = face.getValue();
                if (f.texture != null) {
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
                    cuboididx[facing.ordinal()] = imgidx;
                    btr.setPatchTexture(gtf, tm, imgidx);
                    imgidx++;
                }
            }
            // Add cuboid element to model
            mod.addCuboid(be.from[0]/16.0, be.from[1]/16.0, be.from[2]/16.0, be.to[0]/16.0, be.to[1]/16.0, be.to[2]/16.0, cuboididx);
        }
    }

    public void registerDynmapPatches(String blkname, StateRec state, List<BlockElement> elems, WellKnownBlockClasses type) {
        String[] tok = blkname.split(":");
        String modid = tok[0];
        String blknm = tok[1];
        int[] meta = state.metadata;
        if (tok[0].equals("minecraft")) {   // Skip vanilla
            return;
        }
        // Temporary hack to avoid registering metadata duplicates
        meta = pruneDuplicateMeta(blkname, meta); 
        if (meta.length == 0) {
            return;
        }
        // Get record for mod
        ModDynmapRec td = getModRec(modid);
        // Create block texture record
        BlockTextureRecord btr = td.getBlockTxtRec(blknm, meta);
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
        PatchBlockModel mod = td.getPatchModelRec(blknm, meta);
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
    	
    	if (ovr == null) {	// No override
    		return loadBlockStateFile(modid, respath);
    	}
    	else if (ovr.blockStateName != null) {	// Simple override
    		return loadBlockStateFile(modid, ovr.blockStateName);
    	}
    	else if (ovr.baseNameProperty != null) {	// MUltiple files based on base property
    		List<String> vals = propMap.get(ovr.baseNameProperty);	// Look up defned values
    		if (vals == null) {
    			logger.warning(String.format("%s:%s : bad baseNameProperty=%s",  modid, respath, ovr.baseNameProperty));;
    			return null;
    		}
    		BSBlockState bs = new BSBlockState();
    		bs.nestedProp = ovr.baseNameProperty;
    		bs.nestedValueMap = new HashMap<String, BSBlockState>();
    		for (String v : vals) {
    			BSBlockState bs2 = loadBlockStateFile(modid, v + ovr.nameSuffix);
    			if (bs2 != null) {
    				bs.nestedValueMap.put(v,  bs2);
    			}
    		}
    		return bs;
    	}

		return null;
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
                            if(va.generateElements(models) == false) {
                                logger.fine(va.toString() + ": failed to generate elements for " + blkname + "[" + var.getKey() + "]");
                            }
                            else {
                                elems.addAll(va.elements);
                            }
                        }
                    }
                    // If single simple full cube
                    if ((elems.size() == 1) && (elems.get(0).isSimpleBlock())) {
                        if (br.handler != null) {
                            //logger.info(String.format("%s: %s is simple block with %s map",  blkname, var.getKey(), br.handler.getName()));
                            registerSimpleDynmapCubes(blkname, var.getKey(), elems.get(0), br.sc.getBlockType());
                        }
                    }
                    // Else if simple cuboid
                    else if (isSimpleCuboid(elems)) {
                        if (br.handler != null) {
                            //logger.info(String.format("%s: %s is simple block with %s map",  blkname, var.getKey(), br.handler.getName()));
                            registerSimpleDynmapCuboids(blkname, var.getKey(), elems, br.sc.getBlockType());
                        }
                    }
                    else  {
                        if (br.handler != null) {
                            //logger.info(String.format("%s: %s is simple block with %s map",  blkname, var.getKey(), br.handler.getName()));
                            registerDynmapPatches(blkname, var.getKey(), elems, br.sc.getBlockType());
                        }
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
					models.put(modid, mod.parentModel);
					modelToResolve.push(mod.parentModel);
        		}
        	}
        }
    }

}
