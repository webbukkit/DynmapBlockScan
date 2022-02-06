package org.dynmap.blockscan.forge_1_17_1.model;

import java.util.Arrays;

public class BlockFace {
    public String cullface = null;
    public int tintindex = -1;
    public String texture;
    public float[] uv = null;
    public int rotation = 0;
    
    // From transforms
    public int facerotation = 0;
    
    public BlockFace() {
    }
    
    public BlockFace(BlockFace src, String txt, int rot) {
    	cullface = src.cullface;
    	tintindex = src.tintindex;
    	texture = txt;
    	rotation = src.rotation;
    	facerotation = (src.facerotation + rot) % 360;
    	if (src.uv != null) {
    		uv = Arrays.copyOf(src.uv, src.uv.length);
    	}
    }
    
    // Is full face of cube
    public boolean isFullFace() {
    	if (cullface == null) return false;
    	if (uv != null) {
    		if ((Math.min(uv[0], uv[2]) != 0.0F) || (Math.max(uv[0], uv[2]) != 16.0F) ||
				(Math.min(uv[1], uv[3]) != 0.0F) || (Math.max(uv[1], uv[3]) != 16.0F)) {
    			return false;
    		}
    	}
    	return true;
    }
}
