package org.dynmapblockscan.core.blockstate;

import org.dynmapblockscan.core.blockstate.ModelRotation;

public class ModelRotation {
	public final int rotX, rotY, rotZ;
	
    public ModelRotation(int x, int y, int z) {
    	rotX = x; rotY = y; rotZ = z;
    }
    public static ModelRotation getModelRotation(int x, int y, int z) {
    	return new ModelRotation(x, y, z);
    }
    public boolean isDefault() {
    	return (rotX == 0) && (rotY == 0) && (rotZ == 0);
    }
    public String toString() {
    	return String.format("{ x: %d, y: %d, z = %d }", rotX, rotY, rotZ);
    }
}