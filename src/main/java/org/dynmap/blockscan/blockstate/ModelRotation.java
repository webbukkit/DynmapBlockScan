package org.dynmap.blockscan.blockstate;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.EnumFacing;

public enum ModelRotation {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final Map<Integer, ModelRotation> MAP_ROTATIONS = Maps.<Integer, ModelRotation>newHashMap();
    private final int combinedXY;
    private final int quartersX;
    private final int quartersY;
    private EnumFacing facemap[];	// resulting face after this rotation, indexed by ordinal of initial EnumFace
    private int facerot[];	// Number of CW quarter rotations for face due to rotation
    
    private static int combineXY(int p_177521_0_, int p_177521_1_) {
        return p_177521_0_ * 360 + p_177521_1_;
    }

    private ModelRotation(int x, int y) {
        this.combinedXY = combineXY(x, y);
        this.quartersX = Math.abs(x / 90);
        this.quartersY = Math.abs(y / 90);
        facemap = new EnumFacing[6];
        facerot = new int[6];
        for (EnumFacing f : EnumFacing.VALUES) {
        	facemap[f.ordinal()] = f;
        	facerot[f.ordinal()] = 0;
        }
        // Now rotate by number of quarters around X
        for (int i = 0; i < quartersX; i++) {
        	rotateX();
        }
        // Then rotate around Y
        for (int i = 0; i < quartersY; i++) {
        	rotateY();
        }
    }
    
    private void transferFace(EnumFacing src, EnumFacing dest, int rotation, EnumFacing[] newface, int[] newrot) {
    	newface[dest.ordinal()] = facemap[src.ordinal()];
    	newrot[dest.ordinal()] = (facerot[src.ordinal()] + rotation) % 4;
    }
    private void rotateX() {
    	int[] newrot = new int[6];
    	EnumFacing[] newface = new EnumFacing[6];
    	// Pivot the X sides (W=-X, E=+X)
    	transferFace(EnumFacing.WEST, EnumFacing.WEST, 3, newface, newrot);
    	transferFace(EnumFacing.EAST, EnumFacing.EAST, 1, newface, newrot);
    	// Move others around +X
    	transferFace(EnumFacing.SOUTH, EnumFacing.DOWN, 3, newface, newrot);
    	transferFace(EnumFacing.NORTH, EnumFacing.UP, 3, newface, newrot);
    	transferFace(EnumFacing.UP, EnumFacing.SOUTH, 3, newface, newrot);
    	transferFace(EnumFacing.DOWN, EnumFacing.NORTH, 3, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }
    private void rotateY() {
    	int[] newrot = new int[6];
    	EnumFacing[] newface = new EnumFacing[6];
    	// Pivot the Y sides (D=-Y, U=+Y)
    	transferFace(EnumFacing.UP, EnumFacing.UP, 3, newface, newrot);
    	transferFace(EnumFacing.DOWN, EnumFacing.DOWN, 1, newface, newrot);
    	// Move others around +Y
    	transferFace(EnumFacing.SOUTH, EnumFacing.EAST, 0, newface, newrot);
    	transferFace(EnumFacing.WEST, EnumFacing.SOUTH, 0, newface, newrot);
    	transferFace(EnumFacing.NORTH, EnumFacing.WEST, 0, newface, newrot);
    	transferFace(EnumFacing.EAST, EnumFacing.NORTH, 0, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }

    public EnumFacing rotateFace(EnumFacing facing) {
        return facemap[facing.ordinal()];
    }
    
    public int rotateFaceOrientation(EnumFacing facing) {
    	return 90 * facerot[facing.ordinal()];
    }

    public static ModelRotation getModelRotation(int x, int y) {
        return (ModelRotation)MAP_ROTATIONS.get(Integer.valueOf(combineXY(normalizeAngle(x, 360), normalizeAngle(y, 360))));
    }

    static {
        for (ModelRotation modelrotation : values()) {
            MAP_ROTATIONS.put(Integer.valueOf(modelrotation.combinedXY), modelrotation);
        }
    }
    public static int normalizeAngle(int p_180184_0_, int p_180184_1_)
    {
        return (p_180184_0_ % p_180184_1_ + p_180184_1_) % p_180184_1_;
    }
}