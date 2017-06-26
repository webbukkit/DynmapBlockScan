package org.dynmap.blockscan.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.DynmapBlockScanPlugin;
import org.dynmap.blockscan.blockstate.ModelRotation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.EnumFacing;

// Container for parsed JSON elements from block model
public class BlockElement {
    public float[] from;
    public float[] to;
    public ElementRotation rotation = null;
    public Map<EnumFacing, BlockFace> faces = Collections.emptyMap();
    public boolean shade = true;
    
    public BlockElement() {}
    
    /**
     * Create copy of BlockModel with texture references resolved from
     * provided model
     * @param src - source elements
     * @param txtrefs - source of texture references
     */
    public BlockElement(BlockElement src, TextureReferences txtrefs, ModelRotation mrot, boolean uvlock) {
    	from = Arrays.copyOf(src.from, src.from.length);
    	to = Arrays.copyOf(src.to, src.to.length);
    	if (src.rotation != null) { 
    		rotation = new ElementRotation(src.rotation);
    	}
    	shade = src.shade;
    	// Build resolved copies of faces
    	faces = new HashMap<EnumFacing, BlockFace>();
    	for (Entry<EnumFacing, BlockFace> face : src.faces.entrySet()) {
    		BlockFace f = face.getValue();
    		String v = txtrefs.findTextureByID(f.texture);	// Resolve texture
    		if (v == null) {	
    			DynmapBlockScanPlugin.logger.info("Unresolved texture ref: " + f.texture);
    		}
    		else {
    			faces.put(mrot.rotateFace(face.getKey()), new BlockFace(f, v));
    		}
    	}
    }
    
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
    	for (EnumFacing f : EnumFacing.VALUES) {
    		BlockFace ff = faces.get(f);
    		if ((ff == null) || (ff.isFullFace() == false)) {
    			return false;
    		}
    	}
    	return true;
    }
    
	// Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<BlockElement> {
    	@Override
        public BlockElement deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		BlockElement be = new BlockElement();
    		JsonObject obj = element.getAsJsonObject();
    		if (obj.has("from")) {
    			be.from = context.deserialize(obj.get("from"), float[].class);
    		}
    		if (obj.has("to")) {
    			be.to = context.deserialize(obj.get("to"), float[].class);
    		}
    		if (obj.has("rotation")) {
    			be.rotation = context.deserialize(obj.get("rotation"), ElementRotation.class);
    		}
    		if (obj.has("faces")) {
    			JsonObject f = obj.get("faces").getAsJsonObject();
    			be.faces = new HashMap<EnumFacing, BlockFace>();
    			for (Entry<String, JsonElement> fe : f.entrySet()) {
    				EnumFacing facing = EnumFacing.byName(fe.getKey());
    				if (facing != null) {
    					be.faces.put(facing, context.deserialize(fe.getValue(), BlockFace.class));
    				}
    			}
    		}
    		if (obj.has("shade")) {
    			be.shade = obj.get("shade").getAsBoolean();
    		}
            return be;
        }
    }

}
