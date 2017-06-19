package org.dynmap.blockscan.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.DynmapBlockScanPlugin;

// Container for parsed JSON elements from block model
public class BlockElement {
    public float[] from;
    public float[] to;
    public ElementRotation rotation = null;
    public Map<String, BlockFace> faces = Collections.emptyMap();
    public boolean shade = true;
    
    public BlockElement() {}
    
    /**
     * Create copy of BlockModel with texture references resolved from
     * provided model
     * @param src - source elements
     * @param txtrefs - source of texture references
     */
    public BlockElement(BlockElement src, TextureReferences txtrefs) {
    	from = Arrays.copyOf(src.from, src.from.length);
    	to = Arrays.copyOf(src.to, src.to.length);
    	if (src.rotation != null) { 
    		rotation = new ElementRotation(src.rotation);
    	}
    	shade = src.shade;
    	// Build resolved copies of faces
    	faces = new HashMap<String, BlockFace>();
    	for (Entry<String, BlockFace> face : src.faces.entrySet()) {
    		BlockFace f = face.getValue();
    		String v = txtrefs.findTextureByID(f.texture);	// Resolve texture
    		if (v == null) {	
    			DynmapBlockScanPlugin.logger.info("Unresolved texture ref: " + f.texture);
    		}
    		else {
    			faces.put(face.getKey(), new BlockFace(f, v));
    		}
    	}
    }
    
    private static final String[] FACES = { "up", "down", "north", "south", "east", "west" };
    
    // Test if element is simple, full block
    public boolean isSimpleBlock() {
    	// Check from corner
    	if ((from == null) || (from.length < 3) || (from[0] != 0.0F) || (from[1] != 0.0F) || (from[2] != 0.0F)) {
    		return false;
    	}
    	// Check to corner
    	if ((to == null) || (to.length < 3) || (to[0] != 16.0F) || (to[1] != 16.0F) || (to[2] != 16.0F)) {
    		return false;
    	}
    	// If rotation is zero
    	if ((rotation != null) && (rotation.angle != 0.0)) {
    		return false;
    	}
    	// Number of faces
    	for (String f : FACES) {
    		BlockFace ff = faces.get(f);
    		if ((ff == null) || (ff.isFullFace() == false)) {
    			return false;
    		}
    	}
    	return true;
    }
}
