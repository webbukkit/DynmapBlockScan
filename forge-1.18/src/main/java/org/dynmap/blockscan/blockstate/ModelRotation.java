package org.dynmap.blockscan.blockstate;

import com.google.common.collect.Maps;

import net.minecraft.core.Direction;

import java.util.Map;

import org.dynmap.blockscan.util.Matrix3D;
import org.dynmap.blockscan.util.Vector3D;

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
    private Direction facemap[];	// resulting face after this rotation, indexed by ordinal of initial EnumFace
    private int facerot[];	// Number of CW quarter rotations for face due to rotation
    private Matrix3D transform;
    
    private static int combineXY(int p_177521_0_, int p_177521_1_) {
        return p_177521_0_ * 360 + p_177521_1_;
    }

    private ModelRotation(int x, int y) {
        this.combinedXY = combineXY(x, y);
        this.quartersX = Math.abs(x / 90);
        this.quartersY = Math.abs(y / 90);
        facemap = new Direction[6];
        facerot = new int[6];
        for (Direction f : Direction.values()) {
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
        // Invert map
        Direction[] newface = new Direction[6];
        int[] newrot = new int[6];
        for (int i = 0; i < 6; i++) {
            Direction f = facemap[i];
            newface[f.ordinal()] = Direction.values()[i];
            newrot[f.ordinal()] = facerot[i];
        }
        facemap = newface;
        facerot = newrot;
        // And produce transform matrix
        transform = new Matrix3D();    // Start with identity
        if (quartersX != 0) {   // If doing X rotation
            transform.rotateYZ(90.0 * quartersX);
        }
        if (quartersY != 0) {   // If doing Y rotation
            transform.rotateXZ(90.0 * quartersY);
        }
    }
    
    private void transferFace(Direction src, Direction dest, int rotation, Direction[] newface, int[] newrot) {
    	newface[dest.ordinal()] = facemap[src.ordinal()];
    	newrot[dest.ordinal()] = (facerot[src.ordinal()] + rotation) % 4;
    }
    private void rotateX() {
    	int[] newrot = new int[6];
    	Direction[] newface = new Direction[6];
    	// Pivot the X sides (W=-X, E=+X)
    	transferFace(Direction.WEST, Direction.WEST, 1, newface, newrot);
    	transferFace(Direction.EAST, Direction.EAST, 3, newface, newrot);
    	// Move others around +X
    	transferFace(Direction.SOUTH, Direction.UP, 0, newface, newrot);
    	transferFace(Direction.NORTH, Direction.DOWN, 2, newface, newrot);
    	transferFace(Direction.UP, Direction.NORTH, 2, newface, newrot); 
    	transferFace(Direction.DOWN, Direction.SOUTH, 0, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }
    private void rotateY() {
    	int[] newrot = new int[6];
    	Direction[] newface = new Direction[6];
    	// Pivot the Y sides (D=-Y, U=+Y)
    	transferFace(Direction.UP, Direction.UP, 3, newface, newrot);
    	transferFace(Direction.DOWN, Direction.DOWN, 1, newface, newrot);
    	// Move others around +Y
    	transferFace(Direction.SOUTH, Direction.WEST, 0, newface, newrot);
    	transferFace(Direction.WEST, Direction.NORTH, 0, newface, newrot);
    	transferFace(Direction.NORTH, Direction.EAST, 0, newface, newrot);
    	transferFace(Direction.EAST, Direction.SOUTH, 0, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }

    public Direction rotateFace(Direction facing) {
        return facemap[facing.ordinal()];
    }
    
    public int rotateFaceOrientation(Direction facing) {
    	return 90 * facerot[facing.ordinal()];
    }
    
    // Apply rotation transform to vector 'in place'
    public void transformVector(Vector3D v) {
        transform.transform(v);
    }
    
    public static ModelRotation getModelRotation(int x, int y) {
        ModelRotation rot = (ModelRotation)MAP_ROTATIONS.get(Integer.valueOf(combineXY(normalizeAngle(x, 360), normalizeAngle(y, 360))));
        if (rot == null) {
            rot = ModelRotation.X0_Y0;
        }
        return rot;
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