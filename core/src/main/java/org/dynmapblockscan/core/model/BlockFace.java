package org.dynmapblockscan.core.model;

import java.util.Arrays;

import org.dynmapblockscan.core.model.BlockFace;

public class BlockFace {
    public String cullface = null;
    public int tintindex = -1;
    public String texture;
    public double[] uv = null;
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
    		if ((uv[0] != 0.0F) || (uv[2] != 16.0F) ||
				(uv[1] != 0.0F) || (uv[3] != 16.0F)) {
    			return false;
    		}
    	}
    	return true;
    }
}
