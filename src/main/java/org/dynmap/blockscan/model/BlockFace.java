package org.dynmap.blockscan.model;

import java.util.Arrays;

public class BlockFace {
    public String cullface = null;
    public int tintIndex = -1;
    public String texture;
    public float[] uv = null;
    
    public BlockFace() {}
    
    public BlockFace(BlockFace src, String txt) {
    	cullface = src.cullface;
    	tintIndex = src.tintIndex;
    	texture = txt;
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
